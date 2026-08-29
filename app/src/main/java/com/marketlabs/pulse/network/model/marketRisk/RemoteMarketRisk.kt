package com.marketlabs.pulse.network.model.marketRisk

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// 💡 2026-08-29 revision: `summary` is hard-deleted backend-side (its content is now
// `synthesis.detail`) -- removed here rather than kept nullable, since a stale cached response
// with a live `summary` and no `synthesis` would otherwise still parse and mislead. `synthesis` is
// the same Gemini narrative layer Posture/Positioning already carry (see NetworkSynthesis's own
// doc comment below) -- this domain has no numeric gauge, so it's the only new field.
@JsonClass(generateAdapter = true)
data class NetworkMarketRiskAssessment(
    @Json(name = "timestamp") val lastUpdated: Long? = null,
    @Json(name = "risks") val risks: List<NetworkMarketRiskFactor>? = null,
    @Json(name = "source_narrative") val sourceNarrative: String? = null,
    @Json(name = "synthesis") val synthesis: NetworkSynthesis? = null
)

@JsonClass(generateAdapter = true)
data class NetworkMarketRiskFactor(
    @Json(name = "risk_factor") val riskFactor: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "impact_level") val impactLevel: String? = null,
    @Json(name = "context") val context: String? = null
)

// 💡 Small Gemini-authored narrative layer shared by Posture and Positioning, now also Risks (see
// network/model/posture/NetworkMarketPosture.kt's own copy of this shape for the full doc comment
// on why it's duplicated per-domain rather than a cross-domain shared model, and on why every
// field here is nullable -- including the whole object, which can be entirely absent from a
// keep-previous run). `state` distinguishes the genuine "unavailable" first-run edge case (no
// previous synthesis to fall back to) from a real headline/detail pair -- both null with a real
// `state: "unavailable"` value only happens on that first-run case, never a null `synthesis`
// object itself.
@JsonClass(generateAdapter = true)
data class NetworkSynthesis(
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "detail") val detail: String? = null,
    @Json(name = "generated_at") val generatedAt: Long? = null,
    @Json(name = "model") val model: String? = null,
    @Json(name = "content_flags") val contentFlags: List<String>? = null,
    @Json(name = "state") val state: String? = null
)