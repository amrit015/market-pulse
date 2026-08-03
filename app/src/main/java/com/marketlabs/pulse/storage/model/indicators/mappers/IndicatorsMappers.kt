package com.marketlabs.pulse.storage.model.indicators.mappers

import com.marketlabs.pulse.network.model.indicators.NetworkAiSynthesis
import com.marketlabs.pulse.network.model.indicators.NetworkHorizon
import com.marketlabs.pulse.network.model.indicators.NetworkIndicatorPillar
import com.marketlabs.pulse.network.model.indicators.NetworkMasterGauge
import com.marketlabs.pulse.network.model.indicators.NetworkPillarGlances
import com.marketlabs.pulse.network.model.indicators.NetworkUnifiedMetric
import com.marketlabs.pulse.storage.database.entity.IndicatorsEntity
import com.marketlabs.pulse.storage.model.indicators.DomainAiSynthesis
import com.marketlabs.pulse.storage.model.indicators.DomainHorizon
import com.marketlabs.pulse.storage.model.indicators.DomainIndicatorPillar
import com.marketlabs.pulse.storage.model.indicators.DomainMasterGauge
import com.marketlabs.pulse.storage.model.indicators.DomainPillarGlances
import com.marketlabs.pulse.storage.model.indicators.DomainUnifiedMetric
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.utils.enums.SignalColor
import com.marketlabs.pulse.utils.enums.SubcategoryEnums

// ==========================================
// 🌐 NETWORK TO DOMAIN: AI SYNTHESIS
// ==========================================
fun NetworkAiSynthesis.toDomain(): DomainAiSynthesis? {
    val syncData = this.synthesis ?: return null
    return DomainAiSynthesis(
        timestamp = timestamp ?: 0L,
        overarchingCondition = syncData.overarchingCondition ?: "",
        whatChanged = syncData.whatChanged ?: "",
        pillarGlances = syncData.pillarGlances?.toDomain(),
        shortTerm = syncData.shortTerm?.toDomain(),
        mediumTerm = syncData.mediumTerm?.toDomain(),
        longTerm = syncData.longTerm?.toDomain()
    )
}

fun NetworkPillarGlances.toDomain(): DomainPillarGlances {
    return DomainPillarGlances(
        tactical = tactical ?: "",
        systemicRisk = systemicRisk ?: "",
        valuation = valuation ?: "",
        macro = macro ?: ""
    )
}

fun NetworkHorizon.toDomain(): DomainHorizon {
    return DomainHorizon(
        briefing = briefing ?: "",
        riskLevel = riskLevel ?: "UNKNOWN",
        keyDriver = keyDriver ?: "",
        whatToDo = whatToDo ?: ""
    )
}

// ==========================================
// 🌐 NETWORK TO DOMAIN: UNIFIED PILLAR
// ==========================================
fun NetworkIndicatorPillar.toDomain(): DomainIndicatorPillar {
    return DomainIndicatorPillar(
        timestamp = timestamp ?: 0L,
        masterGauge = masterGauge?.toDomain(),
        metrics = metrics?.map { it.toDomain() } ?: emptyList()
    )
}

fun NetworkMasterGauge.toDomain(): DomainMasterGauge {
    return DomainMasterGauge(
        score = score,
        previousScore = previousScore,
        scoreChange = scoreChange,
        signalText = signalText,
        signalColor = SignalColor.fromString(signalColor),
        trendChangeLabel = trendChangeLabel
    )
}

fun NetworkUnifiedMetric.toDomain(): DomainUnifiedMetric {
    return DomainUnifiedMetric(
        id = id ?: "",
        name = name ?: "Unknown",
        category = category ?: "General",
        subcategory = SubcategoryEnums.fromString(subcategory),
        valueRaw = valueRaw,
        valueDisplay = valueDisplay,
        previousValueRaw = previousValueRaw,
        previousValueDisplay = previousValueDisplay,
        changeRaw = changeRaw,
        changeDisplay = changeDisplay,
        signalText = signalText,
        signalColor = SignalColor.fromString(signalColor),
        releaseDate = releaseDate
    )
}

// ==========================================
// 💾 DOMAIN TO ENTITY (Room Database Write)
// ==========================================
fun MarketIndicators.toEntity(): IndicatorsEntity {
    return IndicatorsEntity(
        dateId = dateId,
        lastSyncedTimestamp = lastSyncedTimestamp,
        aiSynthesis = aiSynthesis,
        tacticalMomentum = tacticalMomentum,
        systemicRisk = systemicRisk,
        valuation = valuation,
        macroVitals = macroVitals
    )
}

// ==========================================
// 💾 ENTITY TO DOMAIN (Room Database Read)
// ==========================================
fun IndicatorsEntity.toDomain(): MarketIndicators {
    return MarketIndicators(
        dateId = dateId,
        lastSyncedTimestamp = lastSyncedTimestamp,
        aiSynthesis = aiSynthesis,
        tacticalMomentum = tacticalMomentum,
        systemicRisk = systemicRisk,
        valuation = valuation,
        macroVitals = macroVitals
    )
}