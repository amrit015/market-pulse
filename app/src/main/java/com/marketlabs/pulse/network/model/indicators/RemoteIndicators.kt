package com.marketlabs.pulse.network.model.indicators

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ============================================================================
// 🚦 PILLAR 1: MARKET PHASE (TRAFFIC LIGHT)
// ============================================================================
@JsonClass(generateAdapter = true)
data class NetworkMarketPhase(
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "market_regime") val marketRegime: String? = null,
    @Json(name = "setup_phase") val setupPhase: String? = null,
    @Json(name = "verdict") val verdict: NetworkPhaseVerdict? = null,
    @Json(name = "signals") val signals: NetworkPhaseSignals? = null,
    @Json(name = "pillars") val pillars: NetworkPhasePillars? = null
)

@JsonClass(generateAdapter = true)
data class NetworkPhaseVerdict(
    @Json(name = "score") val score: Int? = null,
    @Json(name = "previous_score") val previousScore: Int? = null,
    @Json(name = "call") val call: String? = null,
    @Json(name = "action") val action: String? = null,
    @Json(name = "formula") val formula: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkPhaseSignals(
    @Json(name = "trend") val trend: String? = null,
    @Json(name = "health") val health: String? = null,
    @Json(name = "risk") val risk: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkPhasePillars(
    @Json(name = "trend") val trend: NetworkPhaseDetails? = null,
    @Json(name = "health") val health: NetworkPhaseDetails? = null,
    @Json(name = "risk") val risk: NetworkPhaseDetails? = null
)

@JsonClass(generateAdapter = true)
data class NetworkPhaseDetails(
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "indicators") val indicators: List<NetworkIndicatorItem>? = null
)

@JsonClass(generateAdapter = true)
data class NetworkIndicatorItem(
    @Json(name = "name") val name: String? = null,
    @Json(name = "value") val value: String? = null,
    @Json(name = "change_percent") val changePercent: String? = null,
    @Json(name = "signal") val signal: String? = null,
    @Json(name = "signal_color") val signalColor: String? = null,
    @Json(name = "description") val description: String? = null
)

// ============================================================================
// 🏥 PILLAR 2: MACRO VITALS (FRED DATA)
// ============================================================================
@JsonClass(generateAdapter = true)
data class NetworkMacroVitals(
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "metrics") val metrics: NetworkMacroMetrics? = null
)

@JsonClass(generateAdapter = true)
data class NetworkMacroMetrics(
    // Note: The backend uses capitalized keys here
    @Json(name = "Inflation") val inflation: List<NetworkVitalItem>? = null,
    @Json(name = "Labor") val labor: List<NetworkVitalItem>? = null,
    @Json(name = "Growth") val growth: List<NetworkVitalItem>? = null,
    @Json(name = "Policy") val policy: List<NetworkVitalItem>? = null
)

@JsonClass(generateAdapter = true)
data class NetworkVitalItem(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "value") val value: Double? = null,
    @Json(name = "previous_value") val previousValue: Double? = null,
    @Json(name = "display_value") val displayValue: String? = null,
    @Json(name = "change") val change: String? = null,
    @Json(name = "signal_color") val signalColor: String? = null,
    @Json(name = "date") val date: String? = null
)

// ============================================================================
// 🎯 PILLAR 3: MARKET ACTION (TACTICAL SETUP)
// ============================================================================
@JsonClass(generateAdapter = true)
data class NetworkMarketAction(
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "action_zone") val actionZone: NetworkActionZone? = null,
    @Json(name = "raw_metrics") val rawMetrics: NetworkActionMetrics? = null
)

@JsonClass(generateAdapter = true)
data class NetworkActionZone(
    @Json(name = "score") val score: Int? = null,
    @Json(name = "previous_score") val previousScore: Int? = null,
    @Json(name = "score_change") val scoreChange: Int? = null,
    @Json(name = "signal") val signal: String? = null,
    @Json(name = "color") val color: String? = null,
    @Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkActionMetrics(
    @Json(name = "fear_and_greed") val fearAndGreed: NetworkActionMetricItem? = null,
    @Json(name = "put_call_ratio") val putCallRatio: NetworkActionMetricItem? = null,
    @Json(name = "sp500_rsi") val sp500Rsi: NetworkActionMetricItem? = null,
    @Json(name = "sma_extension") val smaExtension: NetworkActionMetricItem? = null
)

@JsonClass(generateAdapter = true)
data class NetworkActionMetricItem(
    @Json(name = "value") val value: String? = null,
    @Json(name = "buy_score_contribution") val buyScoreContribution: Int? = null,
    @Json(name = "signal_color") val signalColor: String? = null
)