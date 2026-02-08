package com.marketlabs.pulse.network.model.summary

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkMarketPulse(
    @param:Json(name = "report_type") val reportType: String? = null,
    @param:Json(name = "timestamp") val timestamp: Long? = null,
    @param:Json(name = "verdict") val verdict: NetworkVerdict? = null,
    @param:Json(name = "lead_stories") val leadStories: List<NetworkNewsItem>? = null,
    @param:Json(name = "macro_mix") val macroMix: List<NetworkMacroItem>? = null,
    @param:Json(name = "domino_effect") val dominoEffect: NetworkDominoEffect? = null,
    @param:Json(name = "market_lookout") val marketLookout: NetworkMarketLookout? = null
)

@JsonClass(generateAdapter = true)
data class NetworkVerdict(
    @param:Json(name = "market_regime") val regime: String? = null,
    @param:Json(name = "setup") val setup: String? = null,
    @param:Json(name = "call") val call: String? = null,
    @param:Json(name = "verdict_text") val analysis: String? = null,
    @param:Json(name = "action") val action: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkNewsItem(
    @param:Json(name = "headline") val headline: String? = null,
    @param:Json(name = "summary") val summary: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkMacroItem(
    @param:Json(name = "headline") val headline: String? = null,
    @param:Json(name = "tag") val tag: String? = null,
    @param:Json(name = "summary") val summary: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkDominoEffect(
    @param:Json(name = "trigger") val trigger: String? = null,
    @param:Json(name = "impact") val impact: String? = null,
    @param:Json(name = "outlook") val outlook: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkMarketLookout(
    @param:Json(name = "outlook") val outlook: String? = null
)