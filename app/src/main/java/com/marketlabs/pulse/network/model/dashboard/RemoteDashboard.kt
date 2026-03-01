package com.marketlabs.pulse.network.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkDashboardResponse(
    @Json(name = "market_state") val marketState: NetworkMarketState,
    @Json(name = "assets") val assets: List<NetworkAssetOverview>
)

/**
 * Maps directly to the 'market_state' document in Firestore
 */
@JsonClass(generateAdapter = true)
data class NetworkMarketState(
    @Json(name = "is_equity_open") var isEquityOpen: Boolean? = null,
    @Json(name = "is_futures_open") var isFuturesOpen: Boolean? = null
)

/**
 * Maps directly to the individual asset documents in Firestore
 */
@JsonClass(generateAdapter = true)
data class NetworkAssetOverview(
    @Json(name = "symbol") val symbol: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "is_inverted") val isInverted: Boolean?,
    @Json(name = "price") val price: Double?,
    @Json(name = "previous_close") val previousClose: Double?,
    @Json(name = "change_percent") val changePercent: Double?,
    @Json(name = "rsi") val rsi: Double?,
    @Json(name = "rsi_status") val rsiStatus: String?,
    @Json(name = "macd_signal") val macdSignal: String?,
    @Json(name = "technical_status") val technicalStatus: String?,
    @Json(name = "ai_verdict") val aiVerdict: String?,
    @Json(name = "sma_20") val sma20: Double?,
    @Json(name = "sma_50") val sma50: Double?,
    @Json(name = "sma_200") val sma200: Double?
)