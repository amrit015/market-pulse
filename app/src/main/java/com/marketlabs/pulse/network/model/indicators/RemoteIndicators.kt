package com.marketlabs.pulse.network.model.indicators

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ==========================================
// 🧠 AI SYNTHESIS PILLAR
// ==========================================
@JsonClass(generateAdapter = true)
data class NetworkAiSynthesis(
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "synthesis") val synthesis: NetworkSynthesisData? = null
)

@JsonClass(generateAdapter = true)
data class NetworkSynthesisData(
    @Json(name = "overarching_condition") val overarchingCondition: String? = null,
    @Json(name = "what_changed") val whatChanged: String? = null,
    @Json(name = "pillar_glances") val pillarGlances: NetworkPillarGlances? = null,
    @Json(name = "short_term") val shortTerm: NetworkHorizon? = null,
    @Json(name = "medium_term") val mediumTerm: NetworkHorizon? = null,
    @Json(name = "long_term") val longTerm: NetworkHorizon? = null
)

@JsonClass(generateAdapter = true)
data class NetworkPillarGlances(
    @Json(name = "tactical") val tactical: String? = null,
    @Json(name = "systemic_risk") val systemicRisk: String? = null,
    @Json(name = "valuation") val valuation: String? = null,
    @Json(name = "macro") val macro: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkHorizon(
    @Json(name = "briefing") val briefing: String? = null,
    @Json(name = "risk_level") val riskLevel: String? = null,
    @Json(name = "key_driver") val keyDriver: String? = null,
    @Json(name = "what_to_do") val whatToDo: String? = null
)

// ==========================================
// 📊 UNIFIED QUANTITATIVE PILLAR
// ==========================================
@JsonClass(generateAdapter = true)
data class NetworkIndicatorPillar(
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "master_gauge") val masterGauge: NetworkMasterGauge? = null,
    @Json(name = "metrics") val metrics: List<NetworkUnifiedMetric>? = null
)

@JsonClass(generateAdapter = true)
data class NetworkMasterGauge(
    @Json(name = "score") val score: Int? = null,
    @Json(name = "previous_score") val previousScore: Int? = null,
    @Json(name = "score_change") val scoreChange: Int? = null,
    @Json(name = "signal_text") val signalText: String? = null,
    @Json(name = "signal_color") val signalColor: String? = null,
    @Json(name = "trend_change_label") val trendChangeLabel: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkUnifiedMetric(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "subcategory") val subcategory: String? = null,
    @Json(name = "value_raw") val valueRaw: Double? = null,
    @Json(name = "value_display") val valueDisplay: String? = null,
    @Json(name = "previous_value_raw") val previousValueRaw: Double? = null,
    @Json(name = "previous_value_display") val previousValueDisplay: String? = null,
    @Json(name = "change_raw") val changeRaw: Double? = null,
    @Json(name = "change_display") val changeDisplay: String? = null,
    @Json(name = "signal_text") val signalText: String? = null,
    @Json(name = "signal_color") val signalColor: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null
)