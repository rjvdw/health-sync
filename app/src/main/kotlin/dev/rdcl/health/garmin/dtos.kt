package dev.rdcl.health.garmin

import kotlinx.serialization.Serializable

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
