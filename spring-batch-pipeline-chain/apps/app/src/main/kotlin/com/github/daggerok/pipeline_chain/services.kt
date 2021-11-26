package com.github.daggerok.pipeline_chain

import com.github.daggerok.payment.client.PaymentClient
import com.github.daggerok.user.client.UserClient
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional //(readOnly = true)
class PaymentsReportService(
    private val userClient: UserClient,
    private val paymentClient: PaymentClient,
    private val paymentsReportRepository: PaymentsReportRepository,
) {

    // @Transactional
    fun loadJobPayments(jobId: Long): List<PaymentsReport> =
        paymentClient.getPayments()
            .map {
                PaymentsReport(
                    jobId = jobId,
                    paymentId = it.id,
                    paymentType = it.type,
                    paymentAmount = it.amount,
                    paymentDateTime = it.createdAt,
                    userId = it.userId,
                )
            }
            .let {
                paymentsReportRepository.saveAll(it)
            }
            .onEach { logger.info { "loaded: $it" } }

    fun getJobPayments(jobId: Long): List<PaymentsReport> =
        paymentsReportRepository.findAllByJobId(jobId)

    private companion object : KLogging()
}
