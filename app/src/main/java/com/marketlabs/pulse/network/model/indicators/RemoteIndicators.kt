package com.marketlabs.pulse.network.model.indicators

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ==========================================
// 🧠 AI SYNTHESIS PILLAR (schema_version 2 -- see market-pulse-backend's
// indicatorSynthesis.ts/synthesisPrompt.ts for the source contract)
// ==========================================
@JsonClass(generateAdapter = true)
data class NetworkAiSynthesis(
    @Json(name = "schema_version") val schemaVersion: Int? = null,
    // 💡 `last_updated` deliberately unmodeled here -- see CLAUDE.md's "last_updated vs
    // timestamp" cross-repo contract note. It's a pre-formatted, Firestore-console-only string
    // whose shape isn't guaranteed to stay a plain JSON string across backend changes; a live
    // ai_synthesis document tripped Moshi on it. `timestamp` (epoch millis) is the only field
    // this app reads for "as of" display.
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "content_flags") val contentFlags: List<String>? = null,
    @Json(name = "synthesis") val synthesis: NetworkSynthesisData? = null
)

@JsonClass(generateAdapter = true)
data class NetworkSynthesisData(
    @Json(name = "executive") val executive: NetworkExecutiveBlock? = null,
    @Json(name = "pillar_scorecard") val pillarScorecard: List<NetworkPillarScorecardEntry>? = null,
    @Json(name = "horizons") val horizons: NetworkHorizons? = null
)

@JsonClass(generateAdapter = true)
data class NetworkExecutiveBlock(
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "alignment_with_macro") val alignmentWithMacro: String? = null,
    @Json(name = "alignment_note") val alignmentNote: String? = null,
    @Json(name = "what_changed") val whatChanged: String? = null,
    @Json(name = "shifts") val shifts: List<NetworkShift>? = null
)

@JsonClass(generateAdapter = true)
data class NetworkShift(
    @Json(name = "metric_id") val metricId: String? = null,
    @Json(name = "direction") val direction: String? = null,
    @Json(name = "note") val note: String? = null
)

// 💡 `contributing_metric_ids` removed 2026-08-22 -- the backend dropped it from assembly (indicator
// synthesis revamp follow-up). Never rendered on this side either, so this is a clean removal, not
// a UI change.
@JsonClass(generateAdapter = true)
data class NetworkPillarScorecardEntry(
    @Json(name = "pillar") val pillar: String? = null,
    @Json(name = "stance") val stance: String? = null,
    @Json(name = "agreement") val agreement: String? = null,
    @Json(name = "one_liner") val oneLiner: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkHorizons(
    @Json(name = "short_term") val shortTerm: NetworkHorizonBlock? = null,
    @Json(name = "medium_term") val mediumTerm: NetworkHorizonBlock? = null,
    @Json(name = "long_term") val longTerm: NetworkHorizonBlock? = null
)

// 💡 `key_drivers` removed 2026-08-22 -- the backend dropped it from the schema, prompt,
// validation, and assembly entirely (indicator synthesis revamp follow-up). `NetworkKeyDriver`
// removed alongside it -- nothing else in this file referenced it.
@JsonClass(generateAdapter = true)
data class NetworkHorizonBlock(
    @Json(name = "posture") val posture: String? = null,
    @Json(name = "time_window") val timeWindow: String? = null,
    @Json(name = "risk_level") val riskLevel: String? = null,
    @Json(name = "what_this_means") val whatThisMeans: String? = null,
    @Json(name = "watch_for") val watchFor: String? = null
)

// ==========================================
// 📊 UNIFIED QUANTITATIVE PILLAR
// ==========================================
@JsonClass(generateAdapter = true)
data class NetworkIndicatorPillar(
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "master_gauge") val masterGauge: NetworkMasterGauge? = null,
    @Json(name = "metrics") val metrics: List<NetworkUnifiedMetric>? = null
)

@JsonClass(generateAdapter = true)
data class NetworkMasterGauge(
    @Json(name = "score") val score: Int? = null,
    @Json(name = "previous_score") val previousScore: Int? = null,
    @Json(name = "score_change") val scoreChange: Int? = null,
    @Json(name = "signal_text") val signalText: String? = null,
    @Json(name = "signal_color") val signalColor: String? = null,
    @Json(name = "trend_change_label") val trendChangeLabel: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkUnifiedMetric(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "subcategory") val subcategory: String? = null,
    @Json(name = "value_raw") val valueRaw: Double? = null,
    @Json(name = "value_display") val valueDisplay: String? = null,
    @Json(name = "previous_value_raw") val previousValueRaw: Double? = null,
    @Json(name = "previous_value_display") val previousValueDisplay: String? = null,
    @Json(name = "change_raw") val changeRaw: Double? = null,
    @Json(name = "change_display") val changeDisplay: String? = null,
    @Json(name = "signal_text") val signalText: String? = null,
    @Json(name = "signal_color") val signalColor: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null
)