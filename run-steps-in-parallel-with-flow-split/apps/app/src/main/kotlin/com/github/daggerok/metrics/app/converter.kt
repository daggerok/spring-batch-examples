package com.github.daggerok.metrics.app

import com.github.doyaaaaaken.kotlincsv.client.ICsvFileWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.stereotype.Component

abstract class AbstractCsvConverter<T> {

    fun convertAndWrite(report: ReportWrapper<T>, targetFile: String) =
        csvWriter().open(targetFileName = targetFile, append = false, write = report.writer())

    fun convertToByteArray(report: ReportWrapper<T>): ByteArray =
        ByteArrayOutputStream(1024)
            .apply { csvWriter().open(ops = this, write = report.writer()) }
            .use { it.toByteArray() }

    protected abstract fun getHeadersRows(): Sequence<List<String>>
    protected abstract fun getTitleRow(): List<String>
    protected abstract fun getDataRow(item: T): List<String>

    private fun ReportWrapper<T>.writer(): ICsvFileWriter.() -> Unit = {
        writeRows(getHeadersRows())
        writeRow()
        writeRow(getTitleRow())
        for (item in chunks) {
            writeRow(getDataRow(item))
        }
    }
}

@StepScope
@Component
class PaymentReportCsvConverter : AbstractCsvConverter<PaymentsReport>() {

    override fun getHeadersRows(): Sequence<List<String>> =
        sequenceOf(
            listOf("PLAYER PAYMENT REPORT")
        )

    override fun getTitleRow(): List<String> =
        listOf(
            "PlayerId",
            "PlayerFullName",
            "PlayerRegistrationDate",
            "PaymentId",
            "PaymentType",
            "PaymentAmount",
            "PaymentDateTime",
        )

    override fun getDataRow(item: PaymentsReport): List<String> =
        listOf(
            "%d".format(item.userId),
            item.userFullName,
            item.userRegistrationDate?.format(DateTimeFormatter.ISO_DATE) ?: "",
            "%s".format(item.paymentId),
            "%s".format(item.paymentType),
            DecimalFormat("#0.##").format(item.paymentAmount),
            DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC)).format(item.paymentDateTime),
        )
}
