package com.github.daggerok.metrics.app

import feign.Param
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "payments_report")
data class PaymentsReport(

    @Id
    @GeneratedValue(strategy = IDENTITY)
    // NOTE: // val id: Long = -1 // this will not work, because entity will seem detached // see line 96 of this:
    // org.hibernate.event.internal.DefaultPersistEventListener.onPersist(org.hibernate.event.spi.PersistEvent, java.util.Map)
    val id: Long? = null,

    val jobId: Long = -1,

    val paymentId: Long = -1,
    val paymentType: String = "",
    val paymentAmount: BigDecimal = BigDecimal.ZERO,
    val paymentDateTime: LocalDateTime? = null,

    val userId: Long = -1,
    // var userFullName: String = "",
    // var userRegistrationDate: LocalDate? = null,
    val userFullName: String = "",
    val userRegistrationDate: LocalDate? = null,
)

interface PaymentsReportRepository : JpaRepository<PaymentsReport, Long> {
    fun findAllByJobId(@Param("jobId") jobId: Long): List<PaymentsReport>
}
