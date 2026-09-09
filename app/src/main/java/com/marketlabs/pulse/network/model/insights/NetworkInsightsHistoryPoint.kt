package com.marketlabs.pulse.network.model.insights

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * `GET /insights/{posture|positioning}/history?metric=<id>&limit=<N>` response -- a plain array,
 * oldest first, not wrapped in an envelope object. Shared by both pillars (Posture's `naaim_exposure`/
 * `dark_pool_index`/`net_liquidity` and Positioning's `retail_sentiment`/`institutional_positioning_*`/
 * `short_interest_*`) since the response shape is byte-for-byte identical between them -- unlike the
 * live-snapshot domain (`NetworkMarketPosture`/`NetworkMarketPositioning`), which stays split per this
 * app's usual vertical-slicing convention because those two really do have different shapes.
 *
 * Deliberately different from `NetworkMetricHistoryPoint` (the Indicators domain's own history point):
 * no `value_display`, no `signal_color` -- this domain's gauges never computed either server-side, so
 * there's nothing to send. `value` has no fixed unit across metrics (a raw percent, a raw share count
 * in the tens of millions, a $ trillions figure...) -- formatting it for display is entirely a client
 * concern here (see `GlossaryDetailScreen`'s own per-metric formatter), not something this DTO carries.
 */
@JsonClass(generateAdapter = true)
data class NetworkInsightsHistoryPoint(
    @Json(name = "date") val date: String?,
    @Json(name = "value") val value: Double?,
    @Json(name = "status") val status: String?
)
