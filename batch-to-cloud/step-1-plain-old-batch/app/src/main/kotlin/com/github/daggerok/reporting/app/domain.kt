package com.github.daggerok.reporting.app

import feign.Param
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

@Entity
@Table(name = "payments_report")
data class PaymentsReport(

    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null,

    val jobId: Long = -1,

    val paymentId: Long = -1,
    val paymentType: String = "",
    val paymentAmount: BigDecimal = BigDecimal.ZERO,
    val paymentDateTime: Instant? = null,

    val userId: Long = -1,
    val userFullName: String = "",
    val userRegistrationDate: LocalDate? = null,
)

interface PaymentsReportRepository : JpaRepository<PaymentsReport, Long> {

    @Transactional
    @Query("delete from PaymentsReport r where r.jobId = :jobId")
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    fun deleteAllInBulkByJobId(@Param("jobId") jobId: Long): Int

    fun findAllByJobId(jobId: Long): List<PaymentsReport>
}
