package com.marketlabs.pulse.storage.model.marketRisk

import com.marketlabs.pulse.utils.enums.RiskImpactLevel

/**
 * Represents the overall AI-generated Market Risk Assessment.
 * Fetched from the "market_insights/current_risks" collection.
 */
// 💡 2026-08-29 revision: `summary` removed (its content is now `synthesis.detail`); `risks` is
// variable-length (backend caps defensively at 8, never a fixed 3-4) -- no count assumption
// anywhere this list is rendered.
data class MarketRiskAssessment(
    val date: String? = null,
    val lastSyncedTimestamp: Long? = null,
    val lastUpdated: Long? = null,
    val risks: List<MarketRiskFactor>? = null,
    val sourceNarrative: String? = null,
    val synthesis: MarketRiskSynthesis? = null
)

/**
 * Represents an individual tail risk identified in the market.
 */
data class MarketRiskFactor(
    val riskFactor: String? = null,
    val category: String? = null,
    val impactLevel: RiskImpactLevel? = null,
    val context: String? = null
)

// 💡 Named MarketRiskSynthesis, matching this domain's existing prefix convention
// (MarketRiskAssessment/MarketRiskFactor) rather than Posture/Positioning's "Domain"-prefixed
// naming -- same field shape as DomainPostureSynthesis/DomainPositioningSynthesis/PlaybookSynthesis,
// kept as its own duplicated class per this app's per-domain vertical-slicing convention (see
// NetworkSynthesis's own doc comment, network/model/marketRisk/RemoteMarketRisk.kt).
data class MarketRiskSynthesis(
    val headline: String?,
    val detail: String?,
    val generatedAt: Long?,
    val contentFlags: List<String>,
    val state: String?
)