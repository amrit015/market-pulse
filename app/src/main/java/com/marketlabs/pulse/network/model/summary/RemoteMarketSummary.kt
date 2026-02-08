package com.marketlabs.pulse.network.model.summary

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkMarketPulse(
    @Json(name = "report_type") val reportType: String? = null,
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "verdict") val verdict: NetworkVerdict? = null,
    @Json(name = "lead_stories") val leadStories: List<NetworkNewsItem>? = null,
    @Json(name = "macro_mix") val macroMix: List<NetworkMacroItem>? = null,
    @Json(name = "domino_effect") val dominoEffect: NetworkDominoEffect? = null,
    @Json(name = "market_lookout") val marketLookout: NetworkMarketLookout? = null
)

@JsonClass(generateAdapter = true)
data class NetworkVerdict(
    @Json(name = "market_regime") val regime: String? = null,
    @Json(name = "setup") val setup: String? = null,
    @Json(name = "call") val call: String? = null,
    @Json(name = "verdict_text") val analysis: String? = null,
    @Json(name = "action") val action: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkNewsItem(
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "summary") val summary: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkMacroItem(
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "tag") val tag: String? = null,
    @Json(name = "summary") val summary: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkDominoEffect(
    @Json(name = "trigger") val trigger: String? = null,
    @Json(name = "impact") val impact: String? = null,
    @Json(name = "outlook") val outlook: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkMarketLookout(
    @Json(name = "outlook") val outlook: String? = null
)