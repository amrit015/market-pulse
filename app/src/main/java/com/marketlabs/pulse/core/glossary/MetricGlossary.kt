package com.marketlabs.pulse.core.glossary

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * One band row in a metric's detail sheet (e.g. Cheap / Fair Value / Expensive for pe_ratio).
 * Deliberately carries no numeric threshold -- the cutoff that decides which band is "current"
 * lives only in the backend's classifierLogic.ts, read live off the metric's own `signal_text`.
 * `label` must stay a valid `signal_text` value for this metric or the current-band highlight in
 * the detail sheet silently stops matching (see MetricGlossaryProvider's header).
 */
@JsonClass(generateAdapter = true)
data class MetricGlossaryBand(
    @Json(name = "label") val label: String,
    @Json(name = "meaning") val meaning: String
)

@JsonClass(generateAdapter = true)
data class MetricGlossaryEntry(
    @Json(name = "what_it_is") val whatItIs: String,
    @Json(name = "how_to_read") val howToRead: String,
    @Json(name = "bands") val bands: List<MetricGlossaryBand> = emptyList(),
    @Json(name = "gotchas") val gotchas: String? = null
)
