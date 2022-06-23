package com.github.daggerok.payment

import com.github.daggerok.payment.PaymentType.DEPOSIT
import com.github.daggerok.payment.PaymentType.NONE
import com.github.daggerok.payment.PaymentType.WITHDRAW
import com.github.daggerok.payment.api.PaymentDTO
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.math.RoundingMode.HALF_UP
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone
import javax.persistence.Entity
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table
import mu.KLogging
import org.hibernate.annotations.CreationTimestamp
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class PaymentApplication

fun main(args: Array<String>) {
    runApplication<PaymentApplication>(*args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.from(ZoneOffset.UTC)))
    }
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

    @LastModifiedDate
    @CreationTimestamp
    @DateTimeFormat(iso = DATE_TIME)
    val createdAt: Instant? = null,
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

    @ExceptionHandler
    fun handleExceptions(e: Throwable) = let {
        val error = e.message ?: "Unknown"
        logger.warn(e) { "Payment service error: $error" }
        ResponseEntity.badRequest().body(mapOf("error" to error))
    }

    private companion object : KLogging()
}
