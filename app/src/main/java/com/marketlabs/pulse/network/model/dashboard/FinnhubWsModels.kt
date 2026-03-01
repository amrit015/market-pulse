package com.marketlabs.pulse.network.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Incoming trade data from Finnhub WebSocket
 * Example: {"data":[{"p":7296.89,"s":"BINANCE:BTCUSDT","t":1575526691134,"v":0.01146847}],"type":"trade"}
 */
@JsonClass(generateAdapter = true)
data class FinnhubTradeResponse(
    @Json(name = "type") val type: String?,
    @Json(name = "data") val data: List<FinnhubTradeData>?
)

@JsonClass(generateAdapter = true)
data class FinnhubTradeData(
    @Json(name = "s") val symbol: String, // e.g., "AAPL" or "BINANCE:BTCUSDT"
    @Json(name = "p") val price: Double,  // Live price
    @Json(name = "t") val timestamp: Long // Unix ms
)

/**
 * Outgoing subscription message we send to Finnhub to start receiving data
 * Example: {"type":"subscribe","symbol":"AAPL"}
 */
@JsonClass(generateAdapter = true)
data class FinnhubSubscribeRequest(
    @Json(name = "type") val type: String = "subscribe",
    @Json(name = "symbol") val symbol: String
)