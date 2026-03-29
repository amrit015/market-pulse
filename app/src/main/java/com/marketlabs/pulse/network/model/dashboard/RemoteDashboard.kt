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
    var isFuturesOpen: Boolean = false,

    // 💡 NEW: Summary fields to be populated from the new Firestore doc
    @Json(name = "summary")
    @get:PropertyName("summary")
    @set:PropertyName("summary")
    var technicalSummary: String? = null,

    @Json(name = "timestamp")
    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var technicalSummaryTimestamp: Long? = null
)

@JsonClass(generateAdapter = true)
data class NetworkAssetOverview(
    var symbol: String = "",
    var name: String = "",
    var type: String = "",
    var description: String = "",
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