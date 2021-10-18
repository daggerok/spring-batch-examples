package com.github.daggerok.pipeline_chain.report

import com.github.daggerok.pipeline_chain.PaymentsReport
import com.github.daggerok.pipeline_chain.PaymentsReportService
import java.util.concurrent.atomic.AtomicBoolean
import mu.KLogging
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@JobScope
@Component
class PaymentsReader(
    @Value("#{jobExecution.jobId}") private val jobId: Long,
    private val paymentsReportService: PaymentsReportService,
) : ItemReader<ReportWrapper<PaymentsReport>> {

    private val itHasBeenRead: AtomicBoolean = AtomicBoolean(false)

    override fun read(): ReportWrapper<PaymentsReport>? =
        if (itHasBeenRead.get()) null
        else paymentsReportService.getJobPayments(jobId)
            .also { itHasBeenRead.set(true) }
            .let {
                ReportWrapper(
                    jobId = jobId,
                    chunks = it
                )
            }
            .also { logger.info { "read: $it" } }

    private companion object : KLogging()
}
