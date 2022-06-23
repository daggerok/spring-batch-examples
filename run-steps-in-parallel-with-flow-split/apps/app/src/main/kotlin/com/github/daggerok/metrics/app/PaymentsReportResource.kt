package com.github.daggerok.metrics.app

import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import mu.KLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentsReportResource(
    private val paymentsReportRepository: PaymentsReportRepository,
    private val asyncLauncher: JobLauncher,
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
            .run { toSpringBatchJob() }

    private companion object : KLogging()
}

data class SpringBatchJob(
    val jobId: Long = -1,
    val jobName: String = "",
    val jobParameters: Map<Any, Any> = mapOf(),
    val jobExecutionId: Long = -1,
    val jobExecutionCreatedAt: Instant = Instant.now(),
    val jobExecutionStartedAt: Instant? = null,
    val jobExecutionEndedAt: Instant? = null,
    val jobExecutionLastModifiedAt: Instant? = null,
)

fun JobExecution.toSpringBatchJob() =
    SpringBatchJob(
        jobId = jobInstance.id,
        jobName = jobInstance.jobName,
        jobParameters = jobParameters.toProperties().toMap(),
        jobExecutionId = id,
        jobExecutionCreatedAt = createTime.toInstant(),
        jobExecutionStartedAt = startTime.toInstant(),         // Looks like a bug, '?' here is required!
        jobExecutionEndedAt = endTime.toInstant(),             // Looks like a bug, '?' here is required!
        jobExecutionLastModifiedAt = lastUpdated.toInstant(),  // Looks like a bug, '?' here is required!
    )
