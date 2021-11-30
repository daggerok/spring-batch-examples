package com.github.daggerok.retry

import com.github.daggerok.payment.client.PaymentClient
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation.REQUIRES_NEW
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true, propagation = REQUIRES_NEW)
class PaymentsReportService(
    private val paymentClient: PaymentClient,
    private val paymentsReportRepository: PaymentsReportRepository,
) {

    @Transactional(readOnly = false, propagation = REQUIRES_NEW)
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
            .let { paymentsReportRepository.saveAll(it) }
            .onEach { logger.info { "loaded: $it" } }

    fun getJobPayments(jobId: Long): List<PaymentsReport> =
        paymentsReportRepository.findAllByJobId(jobId)

    private companion object : KLogging()
}
