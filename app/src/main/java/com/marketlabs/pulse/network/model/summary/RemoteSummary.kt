package com.marketlabs.pulse.network.model.summary

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkMarketPulse(
    @Json(name = "schema_version") val schemaVersion: Int? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "generated_by_model") val generatedByModel: String? = null,
    @Json(name = "report_type") val reportType: String? = null,
    @Json(name = "timestamp") val lastUpdated: Long? = null,
    @Json(name = "verdict") val verdict: NetworkVerdict? = null,
    @Json(name = "drivers") val drivers: List<NetworkDriver>? = null,
    @Json(name = "market_position") val marketPosition: NetworkMarketPosition? = null,
    @Json(name = "lead_stories") val leadStories: List<NetworkNewsItem>? = null,
    @Json(name = "macro_mix") val macroMix: List<NetworkMacroItem>? = null,
    @Json(name = "domino_effect") val dominoEffect: NetworkDominoEffect? = null,
    @Json(name = "watch") val watch: List<NetworkWatchItem>? = null,
    @Json(name = "risks") val risks: List<NetworkRiskItem>? = null,
    @Json(name = "what_changed") val whatChanged: String? = null,
    // 💡 New 2026-08-21: a deterministic (non-AI) list of indicators whose data posted in the
    // last 7 days, most-recent-first. Additive, no route change -- see NetworkWhatsNewEntry.
    @Json(name = "whats_new") val whatsNew: List<NetworkWhatsNewEntry>? = null,
    // 💡 Present in the payload, deliberately never modeled past the mapper -- see
    // SummaryMappers.kt's toDomain(). Declared here (rather than left undeclared, which
    // Moshi would tolerate fine) so their absence from the domain model reads as an
    // explicit decision, not an oversight.
    @Json(name = "rolling_narrative") val rollingNarrative: String? = null,
    @Json(name = "macro_call") val macroCall: NetworkMacroCall? = null,
    @Json(name = "market_outlook") val marketOutlook: NetworkMarketOutlook? = null,
    // 💡 spec-20260902-market-sentiment-android.md: AI-authored cohort-positioning synthesis
    // (retail vs. active managers vs. leveraged futures vs. short-side), additive on the same
    // pulse response -- no new endpoint. Nullable end-to-end so a pre-field or degraded response
    // (either field can independently fail the backend's compliance sanitizer) deserializes
    // cleanly; see SummaryMappers.kt's NetworkMarketSentiment.toDomain() for the null/blank
    // collapse rule.
    @Json(name = "market_sentiment") val marketSentiment: NetworkMarketSentiment? = null
)

@JsonClass(generateAdapter = true)
data class NetworkVerdict(
    @Json(name = "market_regime") val regime: String? = null,
    @Json(name = "setup") val setup: String? = null,
    @Json(name = "signal_line") val signalLine: String? = null,
    @Json(name = "verdict_text") val analysis: String? = null,
    @Json(name = "posture") val posture: String? = null,
    // 💡 The currently deployed Cloud Function still emits the pre-rename `action` key instead
    // of `posture` (confirmed against a live /pulse/v3/latest response) -- kept here so the
    // mapper can fall back to it until the backend redeploys with the rename. Never read
    // anywhere else; posture and action carry the same content under the old/new key name.
    @Json(name = "action") val action: String? = null,
    @Json(name = "direction") val direction: String? = null,
    @Json(name = "conviction") val conviction: String? = null,
    @Json(name = "conviction_reason") val convictionReason: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkDriver(
    @Json(name = "label") val label: String? = null,
    @Json(name = "direction") val direction: String? = null,
    // 💡 New 2026-08-21: the indicator's own natural/deterministic reading, independent of
    // narrative -- e.g. contracting payrolls is always BEARISH here regardless of how the market
    // is trading it that day. See MarketDriver's doc comment in SummaryModels.kt for how this
    // differs from `direction` (the model's reconciled net-effect-on-equities call). Both fields
    // are always present and can legitimately disagree.
    @Json(name = "data_direction") val dataDirection: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "impact") val impact: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkMarketPosition(
    @Json(name = "horizons") val horizons: NetworkHorizons? = null,
    @Json(name = "positioning") val positioning: NetworkPositioning? = null,
    @Json(name = "valuation") val valuation: NetworkValuation? = null,
    @Json(name = "cycle_zone") val cycleZone: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkHorizons(
    @Json(name = "short") val short: NetworkHorizon? = null,
    @Json(name = "medium") val medium: NetworkHorizon? = null,
    @Json(name = "long") val long: NetworkHorizon? = null
)

@JsonClass(generateAdapter = true)
data class NetworkHorizon(
    @Json(name = "risk_level") val riskLevel: String? = null,
    @Json(name = "key_driver") val keyDriver: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkPositioning(
    @Json(name = "pct_from_52w_high") val pctFrom52wHigh: Double? = null,
    @Json(name = "pct_from_52w_low") val pctFrom52wLow: Double? = null,
    @Json(name = "range_position") val rangePosition: Double? = null,
    @Json(name = "extension_percentile_1y") val extensionPercentile1y: Double? = null,
    @Json(name = "window_label") val windowLabel: String? = null,
    @Json(name = "signal_text") val signalText: String? = null,
    @Json(name = "signal_color") val signalColor: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkValuation(
    @Json(name = "pe_percentile_5y") val pePercentile5y: Double? = null
)

@JsonClass(generateAdapter = true)
data class NetworkNewsItem(
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "summary") val summary: String? = null
)

// spec-20260902-market-sentiment-android.md -- same flat headline/summary shape as
// NetworkNewsItem, both AI-authored and already compliance-sanitized backend-side.
@JsonClass(generateAdapter = true)
data class NetworkMarketSentiment(
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "summary") val summary: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkMacroItem(
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "tag") val tag: String? = null,
    @Json(name = "summary") val summary: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkDominoEffect(
    @Json(name = "trigger") val trigger: String? = null,
    @Json(name = "impact") val impact: String? = null,
    @Json(name = "outlook") val outlook: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkWatchItem(
    @Json(name = "label") val label: String? = null,
    @Json(name = "timeframe") val timeframe: String? = null,
    @Json(name = "why") val why: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkRiskItem(
    @Json(name = "risk") val risk: String? = null,
    @Json(name = "severity") val severity: String? = null,
    @Json(name = "note") val note: String? = null
)

// 💡 Never surfaced past the mapper -- see NetworkMarketPulse's macroCall doc comment.
// A self-scoring, frequently-null SPY price prediction the backend tracks against real
// closes for its own accuracy grading, not vetted content for display.
@JsonClass(generateAdapter = true)
data class NetworkMacroCall(
    @Json(name = "basis") val basis: String? = null,
    @Json(name = "direction") val direction: String? = null,
    @Json(name = "level") val level: Double? = null,
    @Json(name = "level_upper") val levelUpper: Double? = null,
    @Json(name = "predicate") val predicate: String? = null,
    @Json(name = "statement") val statement: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkMarketOutlook(
    @Json(name = "summary") val summary: String? = null
)

// 💡 New 2026-08-21: one row of whats_new[] -- a deterministic (non-AI) list of indicators whose
// data posted in the last 7 days, most-recent-first. label/release_date are always present
// backend-side; everything else can be null. Kept fully nullable here anyway, same as every other
// Network* DTO in this file -- this is a Retrofit response body, not a Firestore toObject() model,
// so there's no no-arg-constructor constraint forcing cheap non-null defaults.
@JsonClass(generateAdapter = true)
data class NetworkWhatsNewEntry(
    @Json(name = "label") val label: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "value_display") val valueDisplay: String? = null,
    @Json(name = "change_display") val changeDisplay: String? = null,
    @Json(name = "signal_text") val signalText: String? = null,
    @Json(name = "signal_color") val signalColor: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null
)
