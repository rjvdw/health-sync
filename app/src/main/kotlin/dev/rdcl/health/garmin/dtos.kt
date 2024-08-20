package dev.rdcl.health.garmin

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate

@Serializable
data class GetResponse(
    val dailyWeightSummaries: List<DailyWeightSummary>
)

@Serializable
data class DailyWeightSummary(
    val summaryDate: String,
    val numOfWeightEntries: Int,
    val allWeightMetrics: List<WeightMetric>
)

@Serializable
data class WeightMetric(
    val weight: Double
)

@Serializable
data class SaveRequest(
    val dateTimestamp: String,
    val gmtTimestamp: String,
    val unitKey: String,
    val value: Double
) {
    constructor(date: LocalDate, weight: BigDecimal) : this(
        date.toString() + "T07:00:00.00",
        date.toString() + "T06:00:00.00",
        "kg",
        weight.toDouble()
    )
}
