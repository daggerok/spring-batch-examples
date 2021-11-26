package com.github.daggerok.payment

import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@TestInstance(PER_CLASS)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class PaymentApplicationTests(@Autowired val paymentsRepository: PaymentsRepository) {

    @Test
    fun `should find all payments`() {
        // given
        val payments = paymentsRepository
            .findPaymentsByOrderByCreatedAtDesc()
            .onEach { logger.info { it } }

        // then
        assertThat(payments).hasSizeGreaterThanOrEqualTo(9)
    }

    @Test
    fun `should find user payments`() {
        // given
        val payments = paymentsRepository
            .findPaymentsByUserIdOrderByCreatedAtDesc(0)
            .onEach { logger.info { it } }

        // then
        assertThat(payments).hasSizeGreaterThanOrEqualTo(5)
    }

    @Test
    fun `should find user deposits`() {
        // given
        val payments = paymentsRepository
            .searchUserDeposits(0)
            .onEach { logger.info { it } }

        // then
        assertThat(payments).hasSizeGreaterThanOrEqualTo(1)
    }

    @Test
    fun `should find user withdrawals`() {
        // given
        val payments = paymentsRepository
            .searchUserWithdrawals(0)
            .onEach { logger.info { it } }

        // then
        assertThat(payments).hasSizeGreaterThanOrEqualTo(4)
    }

    companion object : KLogging()
}
