package com.marketlabs.pulse.storage.model.marketRisk

import com.marketlabs.pulse.utils.enums.RiskImpactLevel

/**
 * Represents the overall AI-generated Market Risk Assessment.
 * Fetched from the "market_insights/current_risks" collection.
 */
data class MarketRiskAssessment(
    val date: String? = null,
    val lastSyncedTimestamp: Long? = null,
    val lastUpdated: Long? = null,
    val summary: String? = null,
    val risks: List<MarketRiskFactor>? = null,
    val sourceNarrative: String? = null
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