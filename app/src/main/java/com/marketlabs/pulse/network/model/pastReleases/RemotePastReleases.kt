package com.marketlabs.pulse.network.model.pastReleases

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// 💡 One slot per standard release, keyed by release id (e.g. "cpi_mm") -- always the most recent
// confirmed reading for that release, not a time series. `last_updated` is deliberately not
// modeled: the backend route strips it from the response before returning (see
// api/marketPulse.ts's /dashboard/past-releases handler), so it never actually arrives.
@JsonClass(generateAdapter = true)
data class NetworkPastReleasesResponse(
    @Json(name = "releases") val releases: Map<String, NetworkPastRelease>? = null
)

@JsonClass(generateAdapter = true)
data class NetworkPastRelease(
    @Json(name = "label") val label: String? = null,
    @Json(name = "feed_title") val feedTitle: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "estimate") val estimate: String? = null,
    @Json(name = "previous") val previous: String? = null,
    @Json(name = "actual") val actual: String? = null,
    @Json(name = "actual_confirmed") val actualConfirmed: Boolean? = null,
    @Json(name = "post_release_impact") val postReleaseImpact: String? = null,
    @Json(name = "resolved_at") val resolvedAt: Long? = null
)
