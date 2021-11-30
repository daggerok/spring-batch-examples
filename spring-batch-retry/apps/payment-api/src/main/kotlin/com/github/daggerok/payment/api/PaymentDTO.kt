package com.github.daggerok.payment.api

import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentDTO(
    val id: Long = -1,
    val userId: Long = -1,
    val type: String = "",
    val amount: BigDecimal = BigDecimal.ZERO,
    val createdAt: LocalDateTime? = null,
)
