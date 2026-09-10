package com.marketlabs.pulse.network.model.dashboard

import com.google.firebase.firestore.PropertyName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkDashboardResponse(
    @Json(name = "market_state") val marketState: NetworkMarketState,
    @Json(name = "assets") val assets: List<NetworkAssetOverview>
)

@JsonClass(generateAdapter = true)
data class NetworkMarketState(
    @Json(name = "is_equity_open")
    @get:PropertyName("is_equity_open")
    @set:PropertyName("is_equity_open")
    var isEquityOpen: Boolean = false,

    @Json(name = "is_futures_open")
    @get:PropertyName("is_futures_open")
    @set:PropertyName("is_futures_open")
    var isFuturesOpen: Boolean = false
)

/**
 * The `market_overview/technical_summary` doc's shape post-rewrite: `summary: String?` (deleted
 * outright on the backend, no transition window) is replaced by a nested `synthesis` narrative +
 * a top-level `state` ("unavailable" | "current"), plus the new market-wide `daily_digest`
 * ("what moved today") riding on the same doc.
 */
@JsonClass(generateAdapter = true)
data class NetworkTechnicalSummary(
    @Json(name = "synthesis")
    @get:PropertyName("synthesis")
    @set:PropertyName("synthesis")
    var synthesis: NetworkSynthesis? = null,

    @Json(name = "state")
    @get:PropertyName("state")
    @set:PropertyName("state")
    var state: String? = null,

    @Json(name = "daily_digest")
    @get:PropertyName("daily_digest")
    @set:PropertyName("daily_digest")
    var dailyDigest: NetworkDailyDigest? = null
)

@JsonClass(generateAdapter = true)
data class NetworkSynthesis(
    @Json(name = "headline")
    @get:PropertyName("headline")
    @set:PropertyName("headline")
    var headline: String? = null,

    @Json(name = "detail")
    @get:PropertyName("detail")
    @set:PropertyName("detail")
    var detail: String? = null
)

/** Model-chosen `{category, heading, body}` sections, max 4 -- `category` is a routing field, not a UI kicker. */
@JsonClass(generateAdapter = true)
data class NetworkDailyDigest(
    @Json(name = "sections")
    @get:PropertyName("sections")
    @set:PropertyName("sections")
    var sections: List<NetworkDigestSection>? = null
)

@JsonClass(generateAdapter = true)
data class NetworkDigestSection(
    @Json(name = "category")
    @get:PropertyName("category")
    @set:PropertyName("category")
    var category: String? = null,

    @Json(name = "heading")
    @get:PropertyName("heading")
    @set:PropertyName("heading")
    var heading: String? = null,

    @Json(name = "body")
    @get:PropertyName("body")
    @set:PropertyName("body")
    var body: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkAssetOverview(
    var symbol: String = "",
    var name: String = "",
    var type: String = "",
    var price: Double = 0.0,
    var rsi: Double? = null,
    var timestamp: Long = 0L,

    @Json(name = "is_inverted")
    @get:PropertyName("is_inverted")
    @set:PropertyName("is_inverted")
    var isInverted: Boolean = false,

    @Json(name = "previous_close")
    @get:PropertyName("previous_close")
    @set:PropertyName("previous_close")
    var previousClose: Double? = null,

    @Json(name = "change_percent")
    @get:PropertyName("change_percent")
    @set:PropertyName("change_percent")
    var changePercent: Double? = null,

    @Json(name = "rsi_status")
    @get:PropertyName("rsi_status")
    @set:PropertyName("rsi_status")
    var rsiStatus: String? = null,

    @Json(name = "macd_signal")
    @get:PropertyName("macd_signal")
    @set:PropertyName("macd_signal")
    var macdSignal: String? = null,

    @Json(name = "sma_20")
    @get:PropertyName("sma_20")
    @set:PropertyName("sma_20")
    var sma20: Double? = null,

    @Json(name = "sma_50")
    @get:PropertyName("sma_50")
    @set:PropertyName("sma_50")
    var sma50: Double? = null,

    @Json(name = "sma_200")
    @get:PropertyName("sma_200")
    @set:PropertyName("sma_200")
    var sma200: Double? = null,

    @Json(name = "volume_trend")
    @get:PropertyName("volume_trend")
    @set:PropertyName("volume_trend")
    var volumeTrend: String? = null,

    @Json(name = "technical_status")
    @get:PropertyName("technical_status")
    @set:PropertyName("technical_status")
    var technicalStatus: String? = null
)