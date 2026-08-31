package com.marketlabs.pulse.network.model.weeklyPlaybook

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// 💡 2026-08-29 revision: added `synthesis` -- the same Gemini narrative layer Posture/Positioning/
// Risks already carry (see NetworkSynthesis's own doc comment below). Per-event `actual`/
// `post_release_impact` are unchanged: the backend now sources `actual` from the ForexFactory feed
// itself rather than a search-grounded AI guess, but the Android shape (a plain nullable String,
// no enum) is identical either way.
@JsonClass(generateAdapter = true)
data class NetworkWeeklyPlaybook(
    @Json(name = "timestamp") val lastUpdated: Long? = null,
    @Json(name = "week_starting") val weekStarting: String? = null,
    @Json(name = "events") val events: List<NetworkWeeklyEvent>? = null,
    @Json(name = "synthesis") val synthesis: NetworkSynthesis? = null
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

// 💡 Small Gemini-authored narrative layer shared by Posture and Positioning, now also Risks and
// Events (see network/model/posture/NetworkMarketPosture.kt's own copy of this shape for the full
// doc comment on why it's duplicated per-domain rather than a cross-domain shared model, and on
// why every field here is nullable -- including the whole object, which can be entirely absent
// from a keep-previous run). `state` distinguishes the genuine "unavailable" first-run edge case
// (no previous synthesis to fall back to) from a real headline/detail pair.
@JsonClass(generateAdapter = true)
data class NetworkSynthesis(
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "detail") val detail: String? = null,
    @Json(name = "generated_at") val generatedAt: Long? = null,
    @Json(name = "model") val model: String? = null,
    @Json(name = "content_flags") val contentFlags: List<String>? = null,
    @Json(name = "state") val state: String? = null
)