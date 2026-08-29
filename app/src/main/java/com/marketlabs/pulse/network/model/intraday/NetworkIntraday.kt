package com.marketlabs.pulse.network.model.intraday

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * `GET /intraday/:symbol` response — today's "shape of the day" bars, ~5 minutes apart for
 * stocks or ~1 minute apart for the 23 live-priced dashboard assets (finer resolution there
 * since the underlying poll cadence is finer — see the backend spec's Part B/D6). Not a history:
 * this is a single record per symbol that gets *reset* when a new trading day starts, not
 * appended forever, so `date` must be checked against today before trusting `bars` as current —
 * see `IntradayMappers.kt`'s `toDomain`.
 */
@JsonClass(generateAdapter = true)
data class NetworkIntradayResponse(
    @Json(name = "symbol") val symbol: String?,
    @Json(name = "date") val date: String?,
    @Json(name = "prev_close") val prevClose: Double?,
    @Json(name = "bars") val bars: List<NetworkIntradayBar>?,
    @Json(name = "count") val count: Int?
)

@JsonClass(generateAdapter = true)
data class NetworkIntradayBar(
    @Json(name = "t") val t: String?,
    @Json(name = "price") val price: Double?
)
