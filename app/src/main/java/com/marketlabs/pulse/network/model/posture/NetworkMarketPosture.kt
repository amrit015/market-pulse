package com.marketlabs.pulse.network.model.posture

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// 💡 2026-08-26 revamp: added `synthesis` (Gemini narrative layer) and, on each of the three
// gauges below, the last_observation/delta/delta_direction/fetched_at/stale_since envelope --
// see NetworkLastObservation's own doc comment for why every one of those five fields is
// nullable. `last_updated` is deliberately NOT modeled here: it's a Firestore server-timestamp
// sentinel (serializes as `{_seconds, _nanoseconds}`, not a string or number) that this app has
// never consumed -- the 2026-08-22 indicators revamp already hit this exact bug once by modeling
// it as a String, see this file's cross-repo-contract rule in CLAUDE.md. `timestamp` (epoch
// millis) is the only "as of" field this app reads.
@JsonClass(generateAdapter = true)
data class NetworkMarketPosture(
    @Json(name = "naaim_exposure") val naaimExposure: NetworkNaaimExposure? = null,
    @Json(name = "dark_pool_index") val darkPoolIndex: NetworkDarkPoolIndex? = null,
    @Json(name = "net_liquidity") val netLiquidity: NetworkNetLiquidity? = null,
    @Json(name = "synthesis") val synthesis: NetworkSynthesis? = null,
    @Json(name = "timestamp") val timestamp: Long? = null
)

@JsonClass(generateAdapter = true)
data class NetworkNaaimExposure(
    @Json(name = "value") val value: Double? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "last_observation") val lastObservation: NetworkLastObservation? = null,
    @Json(name = "delta") val delta: Double? = null,
    @Json(name = "delta_direction") val deltaDirection: String? = null,
    @Json(name = "fetched_at") val fetchedAt: Long? = null,
    @Json(name = "stale_since") val staleSince: Long? = null
)

@JsonClass(generateAdapter = true)
data class NetworkDarkPoolIndex(
    @Json(name = "value") val value: Double? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "last_observation") val lastObservation: NetworkLastObservation? = null,
    @Json(name = "delta") val delta: Double? = null,
    @Json(name = "delta_direction") val deltaDirection: String? = null,
    @Json(name = "fetched_at") val fetchedAt: Long? = null,
    @Json(name = "stale_since") val staleSince: Long? = null
)

@JsonClass(generateAdapter = true)
data class NetworkNetLiquidity(
    @Json(name = "value") val value: Double? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "assets_t") val assetsT: Double? = null,
    @Json(name = "tga_t") val tgaT: Double? = null,
    @Json(name = "rrp_t") val rrpT: Double? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "last_observation") val lastObservation: NetworkLastObservation? = null,
    @Json(name = "delta") val delta: Double? = null,
    @Json(name = "delta_direction") val deltaDirection: String? = null,
    @Json(name = "fetched_at") val fetchedAt: Long? = null,
    @Json(name = "stale_since") val staleSince: Long? = null
)

// 💡 Every one of these five fields is nullable, and this is load-bearing, not defensive
// over-caution: naaim_exposure is failing its scrape right now (live-verified 2026-08-26) and its
// current live document has NONE of last_observation/delta/delta_direction/fetched_at -- a total
// fetch failure never writes them at all (gaugeDocument.ts's assembleGauge only ever patches
// `stale_since` on failure, so {merge:true} leaves a gauge that has never once succeeded without
// this whole envelope). `delta` is independently nullable even on a successful fetch: the very
// first-ever observation for a gauge has nothing to diff against, so gaugeDocument.ts's
// computeObservation() sets delta to null that one run (also live-verified today, on
// net_liquidity). last_observation's own value/status/observed_at ARE always set together
// whenever the object itself is present -- computeObservation never emits a partial one.
@JsonClass(generateAdapter = true)
data class NetworkLastObservation(
    @Json(name = "value") val value: Double,
    @Json(name = "status") val status: String,
    @Json(name = "observed_at") val observedAt: Long
)

// 💡 Small Gemini-authored narrative layer shared by Posture and Positioning (each domain's own
// package carries its own copy of this shape -- see NetworkMarketPositioning.kt -- rather than a
// cross-domain shared model file, matching this app's vertical-slicing convention). `headline`/
// `detail` are null on the "unavailable" first-run edge case (no previous synthesis to fall back
// to and the model/sanitizer had nothing usable yet) -- live-verified today on market_posture,
// which currently reads `state: "unavailable"` with both null. The whole `synthesis` object can
// also be entirely absent from the document until the first gauge change after deploy ever
// triggers a synthesis run at all, so NetworkMarketPosture.synthesis above is itself nullable.
@JsonClass(generateAdapter = true)
data class NetworkSynthesis(
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "detail") val detail: String? = null,
    @Json(name = "generated_at") val generatedAt: Long? = null,
    @Json(name = "model") val model: String? = null,
    @Json(name = "content_flags") val contentFlags: List<String>? = null,
    @Json(name = "state") val state: String? = null
)