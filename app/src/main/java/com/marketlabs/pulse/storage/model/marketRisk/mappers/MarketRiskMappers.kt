package com.marketlabs.pulse.storage.model.marketRisk.mappers

import com.marketlabs.pulse.network.model.marketRisk.NetworkMarketRiskAssessment
import com.marketlabs.pulse.network.model.marketRisk.NetworkMarketRiskFactor
import com.marketlabs.pulse.storage.database.entity.MarketRiskEntity
import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskFactor
import com.marketlabs.pulse.utils.enums.RiskImpactLevel

// ============================================================================
// MARKET TAIL RISKS MAPPERS
// ============================================================================

fun NetworkMarketRiskAssessment.toDomain(date: String): MarketRiskAssessment {
    return MarketRiskAssessment(
        date = date, // We inject the date string here so it can be used as the primary key
        lastSyncedTimestamp = System.currentTimeMillis(),
        lastUpdated = this.lastUpdated,
        summary = this.summary,
        risks = this.risks?.map { it.toDomain() },
        sourceNarrative = this.sourceNarrative
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

fun MarketRiskAssessment.toEntity(): MarketRiskEntity {
    return MarketRiskEntity(
        date = this.date ?: "",
        lastSyncedTimestamp = this.lastSyncedTimestamp ?: 0L,
        lastUpdated = this.lastUpdated,
        summary = this.summary,
        risks = this.risks,
        sourceNarrative = this.sourceNarrative
    )
}

fun MarketRiskEntity.toDomain(): MarketRiskAssessment {
    return MarketRiskAssessment(
        date = this.date,
        lastSyncedTimestamp = this.lastSyncedTimestamp,
        lastUpdated = this.lastUpdated,
        summary = this.summary,
        risks = this.risks,
        sourceNarrative = this.sourceNarrative
    )
}