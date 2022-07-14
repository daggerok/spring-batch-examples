package com.github.daggerok.reporting.app

import io.micrometer.core.instrument.MeterRegistry
import mu.KLogging
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.item.ItemWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@JobScope
@Component
class PaymentsWriter(
    private val meter: MeterRegistry,
    @Value("#{jobExecution.jobId}") private val jobId: Long,
    private val paymentReportCsvConverter: PaymentReportCsvConverter,
) : ItemWriter<ReportWrapper<PaymentsReport>> {

    override fun write(items: MutableList<out ReportWrapper<PaymentsReport>>) = meter.measure(this) {
        if (items.size != 1) {
            logger.warn { "Unexpected items size: ${items.size}" }
            return
        }
        paymentReportCsvConverter.convertAndWrite(items.first(), "target/payments-report.csv")
    }

    private companion object : KLogging()
}
