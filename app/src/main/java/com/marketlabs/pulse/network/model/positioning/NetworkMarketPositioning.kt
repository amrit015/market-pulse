package com.marketlabs.pulse.network.model.positioning

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// 💡 Named NetworkMarketPositioning, not the spec doc's literal "NetworkPositioning" -- that name
// already exists in network/model/summary/RemoteSummary.kt for a completely unrelated concept
// (the Summary tab's SPY 52-week "Market Position" gauge). Mirrors this app's existing
// NetworkMarketPosture naming (every layer of that domain is already prefixed MarketPosture) and
// avoids the exact same-word-different-thing confusion the backend's own
// marketPositioningEngine.ts header warns about (market_indicators/positioning is yet a THIRD,
// also-unrelated document -- an SPY 52-week high/low metric, nothing to do with this domain).
//
// Backend route (`GET /insights/positioning`) is genuinely new and, as of 2026-08-26, has never
// successfully returned data -- the scheduled engine (8pm ET weekdays) hasn't completed a run yet,
// confirmed live (curl returns `{"error":"No market positioning data found."}`). Every shape below
// is modeled directly from marketPositioningEngine.ts and its shared utils/gaugeDocument.ts /
// utils/gaugeSynthesis.ts helpers (the same ones market_posture already uses live), not from a
// sampled response -- re-verify field presence once the backend has produced a real document.
//
// `last_updated` is deliberately NOT modeled -- see NetworkMarketPosture.kt's identical note; it's
// a Firestore server-timestamp sentinel, not a string or number, and this app has never consumed
// it. `timestamp` (epoch millis) is the only "as of" field read.
@JsonClass(generateAdapter = true)
data class NetworkMarketPositioning(
    @Json(name = "retail_sentiment") val retailSentiment: NetworkRetailSentiment? = null,
    @Json(name = "institutional_positioning") val institutionalPositioning: NetworkInstitutionalPositioning? = null,
    @Json(name = "short_interest") val shortInterest: NetworkShortInterest? = null,
    @Json(name = "synthesis") val synthesis: NetworkSynthesis? = null,
    @Json(name = "timestamp") val timestamp: Long? = null
)

// 💡 AAII weekly bull/bear/neutral survey. `lastObservation`/`delta`/`deltaDirection`/`fetchedAt`
// are nullable for the same reason as every gauge in NetworkMarketPosture.kt -- a total fetch
// failure never writes them, and the very first-ever observation has a null delta. `staleSince`
// lives alongside them at this same flat level since retail_sentiment is a flat (non-composite)
// gauge, same shape as Posture's three gauges.
//
// 💡 2026-08-27: `description` is being actively removed from Firestore (FieldValue.delete() on
// the backend's next successful run, not just stopped) -- kept here as nullable rather than
// deleted outright since a still-cached pre-removal document could briefly have it, but the app no
// longer reads it; the UI now shows a client-authored string instead (see
// R.string.positioning_retail_sentiment_description).
@JsonClass(generateAdapter = true)
data class NetworkRetailSentiment(
    @Json(name = "bull_pct") val bullPct: Double? = null,
    @Json(name = "bear_pct") val bearPct: Double? = null,
    @Json(name = "neutral_pct") val neutralPct: Double? = null,
    @Json(name = "bull_bear_spread") val bullBearSpread: Double? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "reported_date") val reportedDate: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "last_observation") val lastObservation: NetworkLastObservation? = null,
    @Json(name = "delta") val delta: Double? = null,
    @Json(name = "delta_direction") val deltaDirection: String? = null,
    @Json(name = "fetched_at") val fetchedAt: Long? = null,
    @Json(name = "stale_since") val staleSince: Long? = null
)

// 💡 A composite gauge: `fetchedAt`/`staleSince` live once at THIS group level (one CFTC call
// covers all four contracts as one logical unit), not per instrument -- matching
// marketPositioningEngine.ts's buildInstitutionalPositioningGauge(). `es`/`nq`/`rty`/`dia` are
// individually nullable: Firestore's `{merge:true}` means an instrument that has NEVER once
// succeeded since this document was created is simply absent, even once the others have data
// (buildCotInstrument's per-instrument partial-failure handling). `dia` (E-mini Dow) added
// 2026-08-27, live-verified -- unlike es/nq/rty it isn't reported under CFTC's Legacy
// non-commercial category (Dow futures aren't in that report at all), so it carries a different
// `methodology` value on the contract itself; see `NetworkFuturesContract.methodology`.
// `description` removal note: see `NetworkRetailSentiment`'s identical doc comment.
@JsonClass(generateAdapter = true)
data class NetworkInstitutionalPositioning(
    @Json(name = "es") val es: NetworkFuturesContract? = null,
    @Json(name = "nq") val nq: NetworkFuturesContract? = null,
    @Json(name = "rty") val rty: NetworkFuturesContract? = null,
    @Json(name = "dia") val dia: NetworkFuturesContract? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "fetched_at") val fetchedAt: Long? = null,
    @Json(name = "stale_since") val staleSince: Long? = null
)

// 💡 `ncNetPctOi`/`ncNetContracts`/`status`/`percentile`/`reportDate`/`methodology` are always
// written together in one shot whenever this instrument's own fetch succeeds at all
// (buildCotInstrument's `fields` object) -- non-null is a real invariant, not laziness. Only
// `lastObservation`/`delta`/`deltaDirection` are independently nullable (first-ever observation
// for this specific contract). `pctile_window` (the 52-entry backing array) is intentionally NOT
// modeled -- no UI consumer yet.
//
// 💡 `methodology` (2026-08-27, live-verified: "legacy_non_commercial" for es/nq/rty,
// "tff_leveraged_funds" for dia) -- CFTC doesn't report Dow futures under the Legacy
// non-commercial category at all, so `dia` is sourced from the TFF report's Leveraged Funds
// category instead. Different reporting universe/methodology, so `dia`'s percentile/status aren't
// directly comparable to es/nq/rty's -- surfaced in the UI, not silently blended together.
@JsonClass(generateAdapter = true)
data class NetworkFuturesContract(
    @Json(name = "nc_net_pct_oi") val ncNetPctOi: Double,
    @Json(name = "nc_net_contracts") val ncNetContracts: Long,
    @Json(name = "status") val status: String,
    @Json(name = "percentile") val percentile: Int,
    @Json(name = "report_date") val reportDate: String,
    @Json(name = "methodology") val methodology: String,
    @Json(name = "last_observation") val lastObservation: NetworkLastObservation? = null,
    @Json(name = "delta") val delta: Double? = null,
    @Json(name = "delta_direction") val deltaDirection: String? = null
)

// 💡 Composite gauge, same fetchedAt/staleSince-at-group-level shape as institutional_positioning
// above -- FINRA's short-interest pull covers all six instruments as one logical unit. `dia`/
// `rsp`/`mags` added 2026-08-27, live-verified. `description` removal note: see
// `NetworkRetailSentiment`'s identical doc comment.
@JsonClass(generateAdapter = true)
data class NetworkShortInterest(
    @Json(name = "spy") val spy: NetworkShortInterestInstrument? = null,
    @Json(name = "qqq") val qqq: NetworkShortInterestInstrument? = null,
    @Json(name = "iwm") val iwm: NetworkShortInterestInstrument? = null,
    @Json(name = "dia") val dia: NetworkShortInterestInstrument? = null,
    @Json(name = "rsp") val rsp: NetworkShortInterestInstrument? = null,
    @Json(name = "mags") val mags: NetworkShortInterestInstrument? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "fetched_at") val fetchedAt: Long? = null,
    @Json(name = "stale_since") val staleSince: Long? = null
)

// 💡 `shortShares` (raw share count), NOT `shortPctFloat` -- the FINRA dataset this pulls from has
// no shares-outstanding/float field to compute a percentage against (finraClient.ts). `delta` is
// nullable for the same first-observation reason as everywhere else in this file, even though the
// underlying value is a whole share count -- gaugeDocument.ts's computeObservation() is generic
// across every gauge in both domains and always emits `delta: null` on a metric's first-ever run.
@JsonClass(generateAdapter = true)
data class NetworkShortInterestInstrument(
    @Json(name = "short_shares") val shortShares: Long,
    @Json(name = "days_to_cover") val daysToCover: Double,
    @Json(name = "mom_change_pct") val momChangePct: Double,
    @Json(name = "settlement_date") val settlementDate: String,
    @Json(name = "status") val status: String,
    @Json(name = "last_observation") val lastObservation: NetworkLastObservation? = null,
    @Json(name = "delta") val delta: Double? = null,
    @Json(name = "delta_direction") val deltaDirection: String? = null
)

// 💡 Duplicated from network/model/posture/NetworkMarketPosture.kt rather than shared across
// packages -- see that file's identical class for the full rationale (this app's domains are
// vertically sliced, no cross-domain shared network model files). Same invariant holds here:
// value/status/observedAt are always set together whenever this object is present at all.
@JsonClass(generateAdapter = true)
data class NetworkLastObservation(
    @Json(name = "value") val value: Double,
    @Json(name = "status") val status: String,
    @Json(name = "observed_at") val observedAt: Long
)

// 💡 Duplicated from network/model/posture/NetworkMarketPosture.kt -- see that file's identical
// class for the full rationale.
@JsonClass(generateAdapter = true)
data class NetworkSynthesis(
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "detail") val detail: String? = null,
    @Json(name = "generated_at") val generatedAt: Long? = null,
    @Json(name = "model") val model: String? = null,
    @Json(name = "content_flags") val contentFlags: List<String>? = null,
    @Json(name = "state") val state: String? = null
)
