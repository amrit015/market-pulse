package com.marketlabs.pulse.network.model.riskRadar

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkRiskRadar(
    @Json(name = "timestamp") val lastUpdated: Long? = null,
    @Json(name = "vulnerability_score") val score: Int? = null,
    @Json(name = "previous_score") val previousScore: Int? = null,
    @Json(name = "trend") val trend: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "gauges") val gauges: NetworkRiskGauges? = null
)

@JsonClass(generateAdapter = true)
data class NetworkRiskGauges(
    @Json(name = "recession") val recession: NetworkGauge? = null,
    @Json(name = "foundation") val foundation: NetworkGauge? = null,
    @Json(name = "rotation") val rotation: NetworkGauge? = null,
    @Json(name = "growth_fear") val growthFear: NetworkGauge? = null,
    @Json(name = "canary") val canary: NetworkGauge? = null
)

@JsonClass(generateAdapter = true)
data class NetworkGauge(
    @Json(name = "value") val value: Double? = null,
    @Json(name = "risk_score") val riskScore: Int? = null,
    @Json(name = "label") val label: String? = null
)
