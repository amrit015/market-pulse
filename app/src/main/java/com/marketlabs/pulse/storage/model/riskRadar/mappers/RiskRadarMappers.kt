package com.marketlabs.pulse.storage.model.riskRadar.mappers

import com.marketlabs.pulse.network.model.riskRadar.NetworkGauge
import com.marketlabs.pulse.network.model.riskRadar.NetworkMarketRiskAssessment
import com.marketlabs.pulse.network.model.riskRadar.NetworkMarketRiskFactor
import com.marketlabs.pulse.network.model.riskRadar.NetworkRiskGauges
import com.marketlabs.pulse.network.model.riskRadar.NetworkRiskRadar
import com.marketlabs.pulse.storage.database.entity.MarketRiskAssessmentEntity
import com.marketlabs.pulse.storage.database.entity.RiskRadarEntity
import com.marketlabs.pulse.storage.model.riskRadar.Gauge
import com.marketlabs.pulse.storage.model.riskRadar.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.riskRadar.MarketRiskFactor
import com.marketlabs.pulse.storage.model.riskRadar.RiskGauges
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import com.marketlabs.pulse.utils.enums.RiskImpactLevel
import com.marketlabs.pulse.utils.enums.RiskStatus
import com.marketlabs.pulse.utils.enums.RiskTrend

// ============================================================================
// 1. NETWORK -> DOMAIN (Used by RemoteRiskRadarDataSource)
// ============================================================================
fun NetworkRiskRadar.toDomain(date: String): RiskRadar {
    return RiskRadar(
        date = date,
        lastSyncedTimestamp = System.currentTimeMillis(),
        lastUpdated = this.lastUpdated ?: 0L,
        score = this.score ?: 0,
        previousScore = this.previousScore ?: 0,
        trend = RiskTrend.fromString(this.trend),
        status = RiskStatus.fromString(this.status),
        gauges = this.gauges?.toDomain()
    )
}

private fun NetworkRiskGauges.toDomain(): RiskGauges {
    return RiskGauges(
        recession = this.recession?.toDomain(),
        foundation = this.foundation?.toDomain(),
        rotation = this.rotation?.toDomain(),
        growthFear = this.growthFear?.toDomain(),
        canary = this.canary?.toDomain()
    )
}

private fun NetworkGauge.toDomain(): Gauge {
    return Gauge(
        value = this.value,
        riskScore = this.riskScore ?: 0,
        label = this.label
    )
}


// ============================================================================
// 2. DOMAIN -> ENTITY (Used by LocalRiskRadarDataSource to save to Room)
// ============================================================================
fun RiskRadar.toEntity(): RiskRadarEntity {
    return RiskRadarEntity(
        date = this.date,
        lastSyncedTimestamp = this.lastSyncedTimestamp,
        lastUpdated = this.lastUpdated,
        score = this.score,
        previousScore = this.previousScore,
        // Convert strict Enums back to Strings for the Room database
        trend = this.trend?.name,
        status = this.status?.name,
        gauges = this.gauges
    )
}


// ============================================================================
// 3. ENTITY -> DOMAIN (Used by LocalRiskRadarDataSource to read for UI)
// ============================================================================
fun RiskRadarEntity.toDomain(): RiskRadar {
    return RiskRadar(
        date = this.date,
        lastSyncedTimestamp = this.lastSyncedTimestamp,
        lastUpdated = this.lastUpdated,
        score = this.score,
        previousScore = this.previousScore,
        // Convert Room Strings back into strict Enums for the UI
        trend = RiskTrend.fromString(this.trend),
        status = RiskStatus.fromString(this.status),
        gauges = this.gauges
    )
}

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

fun MarketRiskAssessment.toEntity(): MarketRiskAssessmentEntity {
    return MarketRiskAssessmentEntity(
        date = this.date ?: "",
        lastSyncedTimestamp = this.lastSyncedTimestamp ?: 0L,
        lastUpdated = this.lastUpdated,
        summary = this.summary,
        risks = this.risks,
        sourceNarrative = this.sourceNarrative
    )
}

fun MarketRiskAssessmentEntity.toDomain(): MarketRiskAssessment {
    return MarketRiskAssessment(
        date = this.date,
        lastSyncedTimestamp = this.lastSyncedTimestamp,
        lastUpdated = this.lastUpdated,
        summary = this.summary,
        risks = this.risks,
        sourceNarrative = this.sourceNarrative
    )
}