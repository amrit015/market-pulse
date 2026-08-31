package com.marketlabs.pulse.storage.model.marketRisk.mappers

import com.marketlabs.pulse.network.model.marketRisk.NetworkMarketRiskAssessment
import com.marketlabs.pulse.network.model.marketRisk.NetworkMarketRiskFactor
import com.marketlabs.pulse.network.model.marketRisk.NetworkSynthesis
import com.marketlabs.pulse.storage.database.entity.MarketRiskEntity
import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskFactor
import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskSynthesis
import com.marketlabs.pulse.utils.enums.RiskImpactLevel

// ============================================================================
// MARKET TAIL RISKS MAPPERS
// ============================================================================

fun NetworkMarketRiskAssessment.toDomain(date: String): MarketRiskAssessment {
    return MarketRiskAssessment(
        date = date, // We inject the date string here so it can be used as the primary key
        lastSyncedTimestamp = System.currentTimeMillis(),
        lastUpdated = this.lastUpdated,
        risks = this.risks?.map { it.toDomain() },
        sourceNarrative = this.sourceNarrative,
        synthesis = this.synthesis?.toDomain()
    )
}

private fun NetworkMarketRiskFactor.toDomain(): MarketRiskFactor {
    return MarketRiskFactor(
        riskFactor = this.riskFactor,
        category = this.category,
        impactLevel = RiskImpactLevel.fromString(this.impactLevel),
        context = this.context
    )
}

private fun NetworkSynthesis.toDomain(): MarketRiskSynthesis {
    return MarketRiskSynthesis(
        headline = headline,
        detail = detail,
        generatedAt = generatedAt,
        contentFlags = contentFlags ?: emptyList(),
        state = state
    )
}

fun MarketRiskAssessment.toEntity(): MarketRiskEntity {
    return MarketRiskEntity(
        date = this.date ?: "",
        lastSyncedTimestamp = this.lastSyncedTimestamp ?: 0L,
        lastUpdated = this.lastUpdated,
        risks = this.risks,
        sourceNarrative = this.sourceNarrative,
        synthesisHeadline = this.synthesis?.headline,
        synthesisDetail = this.synthesis?.detail,
        synthesisGeneratedAt = this.synthesis?.generatedAt,
        synthesisContentFlags = this.synthesis?.contentFlags,
        synthesisState = this.synthesis?.state
    )
}

fun MarketRiskEntity.toDomain(): MarketRiskAssessment {
    return MarketRiskAssessment(
        date = this.date,
        lastSyncedTimestamp = this.lastSyncedTimestamp,
        lastUpdated = this.lastUpdated,
        risks = this.risks,
        sourceNarrative = this.sourceNarrative,
        // 💡 Keyed off `state`, not `headline` -- matches MarketPostureEntity.toDomain()'s
        // identical reconstruction, see that mapper's own doc comment for why (the "unavailable"
        // first-run case has a real synthesis object with a null headline/detail but a non-null
        // `state`).
        synthesis = this.synthesisState?.let {
            MarketRiskSynthesis(
                headline = this.synthesisHeadline,
                detail = this.synthesisDetail,
                generatedAt = this.synthesisGeneratedAt,
                contentFlags = this.synthesisContentFlags ?: emptyList(),
                state = this.synthesisState
            )
        }
    )
}