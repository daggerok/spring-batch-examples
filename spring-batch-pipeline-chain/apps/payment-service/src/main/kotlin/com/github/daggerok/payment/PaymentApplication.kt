package com.github.daggerok.payment

import com.github.daggerok.payment.PaymentType.DEPOSIT
import com.github.daggerok.payment.PaymentType.NONE
import com.github.daggerok.payment.PaymentType.WITHDRAW
import com.github.daggerok.payment.api.PaymentDTO
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.math.RoundingMode.HALF_UP
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.AUTO
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table
import mu.KLogging
import org.hibernate.annotations.CreationTimestamp
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class PaymentApplication

fun main(args: Array<String>) {
    runApplication<PaymentApplication>(*args)
}

enum class PaymentType {
    NONE,
    DEPOSIT,
    WITHDRAW
}

@Entity
@Table(name = "payments")
data class Payment(

    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long = -1,

    val userId: Long = -1,

    @Enumerated(STRING)
    val type: PaymentType = NONE,

    val amount: BigDecimal = ZERO,

    @CreationTimestamp
    val createdAt: LocalDateTime? = null,
)

interface PaymentsRepository : JpaRepository<Payment, Long> {

    fun findPaymentsByOrderByCreatedAtDesc(): List<Payment>

    fun findPaymentsByUserIdOrderByCreatedAtDesc(@Param("userId") userId: Long): List<Payment>

    fun findPaymentsByUserIdAndTypeOrderByCreatedAtDesc(
        @Param("userId") userId: Long,
        @Param("type") type: PaymentType = NONE
    ): List<Payment>
}

fun PaymentsRepository.searchUserDeposits(userId: Long): List<Payment> =
    findPaymentsByUserIdAndTypeOrderByCreatedAtDesc(userId = userId, type = DEPOSIT)

fun PaymentsRepository.searchUserWithdrawals(userId: Long): List<Payment> =
    findPaymentsByUserIdAndTypeOrderByCreatedAtDesc(userId = userId, type = WITHDRAW)

fun Payment.toDTO(): PaymentDTO =
    PaymentDTO(id, userId, type.name, amount.setScale(2, HALF_UP), createdAt)

fun List<Payment>.toDTO(): List<PaymentDTO> =
    map { it.toDTO() }

@RestController
class PaymentsResource(private val paymentsRepository: PaymentsRepository) {

    @GetMapping("/api/payments")
    fun getPayments(): List<PaymentDTO> =
        paymentsRepository.findPaymentsByOrderByCreatedAtDesc()
            .toDTO()

    @GetMapping("/api/payments/user/{userId}")
    fun getUserPayments(@PathVariable("userId") userId: Long): List<PaymentDTO> =
        paymentsRepository.findPaymentsByUserIdOrderByCreatedAtDesc(userId)
            .toDTO()

    @ExceptionHandler
    fun handleExceptions(e: Throwable) = let {
        val error = e.message ?: "Unknown"
        logger.warn(e) { "Payment service error: $error" }
        ResponseEntity.badRequest().body(mapOf("error" to error))
    }

    private companion object : KLogging()
}
