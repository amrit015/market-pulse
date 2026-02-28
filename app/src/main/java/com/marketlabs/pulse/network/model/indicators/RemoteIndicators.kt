package com.marketlabs.pulse.network.model.indicators

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkPhaseSummary(
    @Json(name = "market_regime") val marketRegime: String? = null,
    @Json(name = "setup_phase") val setupPhase: String? = null,
    @Json(name = "verdict") val verdict: NetworkVerdict? = null,
    @Json(name = "signals") val signals: NetworkSignals? = null
)

@JsonClass(generateAdapter = true)
data class NetworkVerdict(
    @Json(name = "score") val score: Int? = null,
    @Json(name = "call") val call: String? = null,
    @Json(name = "action") val action: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkSignals(
    @Json(name = "trend") val trend: String? = null,
    @Json(name = "health") val health: String? = null,
    @Json(name = "risk") val risk: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkPhaseDetails(
    @Json(name = "phase_name") val phaseName: String? = null,
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