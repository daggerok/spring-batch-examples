package com.github.daggerok.reporting.app

import io.micrometer.core.instrument.MeterRegistry
import java.util.concurrent.atomic.AtomicBoolean
import mu.KLogging
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@JobScope
@Component
class PaymentsReader(
    private val meter: MeterRegistry,
    @Value("#{jobExecution.jobId}") private val jobId: Long,
    private val paymentsReportService: PaymentsReportService,
) : ItemReader<ReportWrapper<PaymentsReport>> {

    private val itHasBeenRead: AtomicBoolean = AtomicBoolean(false)

    override fun read(): ReportWrapper<PaymentsReport>? = meter.measure(this) {
        if (itHasBeenRead.get()) null
        else paymentsReportService.getReportRows(jobId)
            .also { itHasBeenRead.set(true) }
            .let {
                ReportWrapper(
                    jobId = jobId,
                    chunks = it,
                )
            }
            .also { logger.info { "read: $it" } }
    }

    private companion object : KLogging()
}
