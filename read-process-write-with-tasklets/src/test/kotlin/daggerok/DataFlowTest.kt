package daggerok

import java.math.BigDecimal
import java.math.RoundingMode.HALF_UP
import java.text.DecimalFormat
import org.junit.jupiter.api.Test

class DataFlowTest {

    val mutableList = mutableListOf(
        "transaction-id-01" to ("player-id-1" to BigDecimal(312.34)),
        "transaction-id-02" to ("player-id-1" to BigDecimal(124.34)),
        "transaction-id-03" to ("player-id-2" to BigDecimal(1124.54)),
        "transaction-id-04" to ("player-id-2" to BigDecimal(24.53)),
        "transaction-id-05" to ("player-id-1" to BigDecimal(1240.53)),
        "transaction-id-06" to ("player-id-1" to BigDecimal(40.53)),
        "transaction-id-07" to ("player-id-2" to BigDecimal(442.53)),
        "transaction-id-08" to ("player-id-2" to BigDecimal(442.53)),
        "transaction-id-09" to ("player-id-1" to BigDecimal(443.32)),
        "transaction-id-10" to ("player-id-1" to BigDecimal(4.30)),
        "transaction-id-11" to ("player-id-2" to BigDecimal(456.00)),
        "transaction-id-12" to ("player-id-2" to BigDecimal(456.00)),
        "transaction-id-13" to ("player-id-1" to BigDecimal(456.11)),
        "transaction-id-14" to ("player-id-1" to BigDecimal(600.09)),
        "transaction-id-15" to ("player-id-2" to BigDecimal(600.08)),
        "transaction-id-16" to ("player-id-3" to BigDecimal(990.17)),
        "transaction-id-17" to ("player-id-5" to BigDecimal(900.27)),
        "transaction-id-18" to ("player-id-3" to BigDecimal(800.36)),
        "transaction-id-19" to ("player-id-4" to BigDecimal(700.45)),
    )

    @Test
    fun `should iterate`() {
        generateSequence(0) { it + 1 }
            .map { mutableList.removeFirstOrNull() }
            .takeWhile { it != null }
            .groupBy { it?.second?.first }
            .filter {
                val playerTransactions: List<Pair<String, Pair<String, BigDecimal>>?> = it.value
                val transactions: List<Pair<String, BigDecimal>?> = playerTransactions.map { it?.second }
                // val total: BigDecimal = transactions.filterNotNull().map { it.second }.fold(BigDecimal.ZERO, BigDecimal::add)
                val total: BigDecimal = transactions.filterNotNull().sumOf { it.second }
                total > 3500.toBigDecimal()
            }
            .forEach { (playerUid, playerTransactions) ->
                val bigDecimalFormatter = DecimalFormat("#.##").apply {
                    minimumFractionDigits = 2
                    roundingMode = HALF_UP
                }
                println("Total($playerUid): ${bigDecimalFormatter.format(playerTransactions.filterNotNull().sumOf { it.second.second })}")
                playerTransactions.filterNotNull().forEach {
                    println(" Transaction(${it.first}): ${bigDecimalFormatter.format(it.second.second)}")
                }
            }
    }
}
