package com.marketlabs.pulse.network.model.charts

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * `GET /charts/:symbol` response — a daily closing-price series backing every period chart
 * (5D/1M/6M/YTD/1Y) in the app. One point per trading day, oldest first, no weekend/holiday gaps.
 * Works for stocks and every dashboard asset class alike (indices, sectors, VIX, futures,
 * commodities, crypto, sentiment) — the backend's `market_charts` collection is written for all
 * of them, so this DTO isn't stock-specific despite living in the same request as `/stocks/...`.
 *
 * `last_updated` (a pre-formatted, not-reliably-a-plain-JSON-string display string) is
 * deliberately left unmodeled here, same as `NetworkStockPreview`/`NetworkAiSynthesis` — see this
 * project's cross-repo-contracts rule on the `last_updated`/`timestamp` pair. Chart consumers only
 * need `daily_closes` and `count`.
 */
@JsonClass(generateAdapter = true)
data class NetworkChartResponse(
    @Json(name = "symbol") val symbol: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "daily_closes") val dailyCloses: List<NetworkChartPoint>?,
    @Json(name = "count") val count: Int?
)

@JsonClass(generateAdapter = true)
data class NetworkChartPoint(
    @Json(name = "date") val date: String?,
    @Json(name = "price") val price: Double?
)
