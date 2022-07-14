package com.github.daggerok.reporting.app

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import mu.KLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentsReportResource(
    private val paymentsReportService: PaymentsReportService,
    private val asyncLauncher: JobLauncher,
    private val paymentsReportJob: Job,
) {

    @GetMapping("/api")
    fun getReports() =
        paymentsReportService.getAll()

    @PostMapping("/api/launch-payments-report")
    fun launchUsersPaymentsReport() =
        JobParametersBuilder()
            .addString("dateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
            .toJobParameters()
            .let { asyncLauncher.run(paymentsReportJob, it) }
            .also { logger.info { "execute job ${it.jobId} with parameters ${it.jobParameters.toProperties()}" } }
            .run { jobId }

    private companion object : KLogging()
}
