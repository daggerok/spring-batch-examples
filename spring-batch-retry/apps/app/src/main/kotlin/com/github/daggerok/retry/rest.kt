package com.github.daggerok.retry

import com.github.daggerok.retry.report.ErrorDocument
import com.github.daggerok.retry.report.LaunchDocument
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import mu.KLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.JobOperator
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentsReportResource(
    private val paymentsReportRepository: PaymentsReportRepository,
    private val asyncLauncher: JobLauncher,
    private val jobOperator: JobOperator,
    private val paymentsReportJob: Job,
) {

    @GetMapping("/api")
    fun getReports(): MutableList<PaymentsReport> =
        paymentsReportRepository.findAll()

    @PostMapping("/api/launch-payments-report")
    fun launchUsersPaymentsReport() =
        JobParametersBuilder()
            .addString("dateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
            .toJobParameters()
            .let { asyncLauncher.run(paymentsReportJob, it) }
            .also { logger.info { "execute job ${it.jobId} with parameters ${it.jobParameters.toProperties()}" } }
            .run { LaunchDocument(jobId) }

    @GetMapping("/api/summary/{jobId}")
    fun getJobSummary(@PathVariable jobId: Long) =
        kotlin
            .runCatching {
                mapOf(
                    "job" to jobOperator.getSummary(jobId),
                    "steps" to jobOperator.getStepExecutionSummaries(jobId),
                )
            }
            .onFailure { logger.warn { it.message } }
            .let {
                val error = it.exceptionOrNull()?.message
                if (it.isFailure) return@let ResponseEntity.badRequest().body(ErrorDocument(error))

                val result = it.getOrNull()
                ResponseEntity.accepted().body(result)
            }

    private companion object : KLogging()
}
