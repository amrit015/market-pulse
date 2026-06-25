package com.marketlabs.pulse.network.model.marketRisk

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class NetworkMarketRiskAssessment(
    @Json(name = "timestamp") val lastUpdated: Long? = null,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "risks") val risks: List<NetworkMarketRiskFactor>? = null,
    @Json(name = "source_narrative") val sourceNarrative: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkMarketRiskFactor(
    @Json(name = "risk_factor") val riskFactor: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "impact_level") val impactLevel: String? = null,
    @Json(name = "context") val context: String? = null
)