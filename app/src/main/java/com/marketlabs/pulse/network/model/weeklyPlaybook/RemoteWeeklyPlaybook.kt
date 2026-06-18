package com.marketlabs.pulse.network.model.weeklyPlaybook

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkWeeklyPlaybook(
    @Json(name = "timestamp") val lastUpdated: Long? = null,
    @Json(name = "week_starting") val weekStarting: String? = null,
    @Json(name = "events") val events: List<NetworkWeeklyEvent>? = null
)

@JsonClass(generateAdapter = true)
data class NetworkWeeklyEvent(
    @Json(name = "event_name") val eventName: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "actual") val actual: String? = null,
    @Json(name = "estimate") val estimate: String? = null,
    @Json(name = "previous") val previous: String? = null,
    @Json(name = "market_context") val marketContext: String? = null,
    @Json(name = "post_release_impact") val postReleaseImpact: String? = null
)