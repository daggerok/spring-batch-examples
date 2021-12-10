package dagggerok.payments

import dagggerok.payments.PaymentType.DEPOSIT
import dagggerok.payments.PaymentType.NONE
import dagggerok.payments.PaymentType.WITHDRAW
import feign.Logger
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.math.RoundingMode.HALF_UP
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table
import mu.KLogging
import org.hibernate.annotations.CreationTimestamp
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Configuration
class PaymentsConfig

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
        @Param("userId") userId: Long, @Param("type") type: PaymentType = NONE,
    ): List<Payment>
}

fun PaymentsRepository.searchUserDeposits(userId: Long): List<Payment> =
    findPaymentsByUserIdAndTypeOrderByCreatedAtDesc(userId = userId, type = DEPOSIT)

fun PaymentsRepository.searchUserWithdrawals(userId: Long): List<Payment> =
    findPaymentsByUserIdAndTypeOrderByCreatedAtDesc(userId = userId, type = WITHDRAW)

data class PaymentDTO(
    val id: Long = -1,
    val userId: Long = -1,
    val type: String = "",
    val amount: BigDecimal = BigDecimal.ZERO,
    val createdAt: LocalDateTime? = null,
)

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
        logger.warn/*(e)*/ { "Payment service error: $error" }
        ResponseEntity.badRequest().body(mapOf("error" to error))
    }

    private companion object : KLogging()
}

@Configuration
@ConditionalOnMissingClass
@EnableFeignClients(clients = [PaymentClient::class])
class PaymentClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun feignLoggerLevel(): Logger.Level =
        Logger.Level.FULL;
}

@FeignClient(
    name = "payment-client",
    url = "http://127.0.0.1:\${server.port}",
)
interface PaymentClient {

    @GetMapping("/api/payments")
    fun getPayments(): List<PaymentDTO>

    @GetMapping("/api/payments/user/{userId}")
    fun getUserPayments(@PathVariable("userId") userId: Long): List<PaymentDTO>
}
