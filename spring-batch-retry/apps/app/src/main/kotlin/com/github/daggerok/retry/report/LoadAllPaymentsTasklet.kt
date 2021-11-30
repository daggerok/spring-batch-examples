package com.github.daggerok.retry.report

import com.github.daggerok.retry.PaymentsReport
import com.github.daggerok.retry.PaymentsReportService
import mu.KLogging
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component

@JobScope
@Component
class LoadAllPaymentsTasklet(
    private val retryTemplate: RetryTemplate,
    private val paymentsReportService: PaymentsReportService,
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? = run {
        val jobId = chunkContext.stepContext.jobInstanceId
        logger.info { "Executing loadAllPayments(jobId=$jobId)" }
        val result = retryTemplate.execute<List<PaymentsReport>, Throwable> {
            logger.info { "A try..." }
            paymentsReportService.loadJobPayments(jobId)
        }
        logger.info { "A try result.size: ${result.size}" }
        RepeatStatus.FINISHED
    }

    private companion object : KLogging()
}
