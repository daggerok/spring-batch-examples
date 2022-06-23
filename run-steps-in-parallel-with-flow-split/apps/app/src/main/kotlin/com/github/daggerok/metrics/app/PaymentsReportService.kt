package com.github.daggerok.metrics.app

import com.github.daggerok.payment.api.PaymentDTO
import com.github.daggerok.payment.client.PaymentClient
import com.github.daggerok.user.api.UserDTO
import com.github.daggerok.user.client.UserClient
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation.REQUIRES_NEW
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(propagation = REQUIRES_NEW, readOnly = true)
class PaymentsReportService(
    private val userClient: UserClient,
    private val paymentClient: PaymentClient,
    private val paymentsReportRepository: PaymentsReportRepository,
) {

    fun loadAllPayments(): List<PaymentDTO> =
        paymentClient.getPayments()
            .also { logger.info { "loadAllPayments: size=${it.size}" } }

    fun loadAllUsers(): List<UserDTO> =
        userClient.getUsers()
            .also { logger.info { "loadAllUsers: size=${it.size}" } }

    @Transactional(propagation = REQUIRES_NEW, readOnly = false)
    fun addItem(item: PaymentsReport) =
        paymentsReportRepository.save(item)

    @Transactional(propagation = REQUIRES_NEW, readOnly = false)
    fun dropReportRowsIfAny(jobId: Long): Int =
        paymentsReportRepository.deleteAllInBulkByJobId(jobId)
            .also { logger.info { "dropReportRowsIfAny(jobId=$jobId): size=$it" } }

    fun getReportRows(jobId: Long): List<PaymentsReport> =
        paymentsReportRepository.findAllByJobId(jobId)
            .also { logger.info { "getReportRows(jobId=$jobId): size=${it.size}" } }

    private companion object : KLogging()
}
