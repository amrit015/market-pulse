package com.marketlabs.pulse.storage.model.indicators.mappers

import com.marketlabs.pulse.network.model.indicators.NetworkAiSynthesis
import com.marketlabs.pulse.network.model.indicators.NetworkExecutiveBlock
import com.marketlabs.pulse.network.model.indicators.NetworkHorizonBlock
import com.marketlabs.pulse.network.model.indicators.NetworkHorizons
import com.marketlabs.pulse.network.model.indicators.NetworkIndicatorPillar
import com.marketlabs.pulse.network.model.indicators.NetworkMasterGauge
import com.marketlabs.pulse.network.model.indicators.NetworkPillarScorecardEntry
import com.marketlabs.pulse.network.model.indicators.NetworkShift
import com.marketlabs.pulse.network.model.indicators.NetworkUnifiedMetric
import com.marketlabs.pulse.storage.database.entity.IndicatorsEntity
import com.marketlabs.pulse.storage.model.indicators.DomainAiSynthesis
import com.marketlabs.pulse.storage.model.indicators.DomainExecutiveBlock
import com.marketlabs.pulse.storage.model.indicators.DomainHorizonBlock
import com.marketlabs.pulse.storage.model.indicators.DomainHorizons
import com.marketlabs.pulse.storage.model.indicators.DomainIndicatorPillar
import com.marketlabs.pulse.storage.model.indicators.DomainMasterGauge
import com.marketlabs.pulse.storage.model.indicators.DomainPillarScorecardEntry
import com.marketlabs.pulse.storage.model.indicators.DomainShift
import com.marketlabs.pulse.storage.model.indicators.DomainUnifiedMetric
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.utils.enums.AgreementState
import com.marketlabs.pulse.utils.enums.AlignmentState
import com.marketlabs.pulse.utils.enums.IndicatorCategory
import com.marketlabs.pulse.utils.enums.RiskImpactLevel
import com.marketlabs.pulse.utils.enums.ShiftDirection
import com.marketlabs.pulse.utils.enums.SignalColor
import com.marketlabs.pulse.utils.enums.SubcategoryEnums

// ==========================================
// 🌐 NETWORK TO DOMAIN: AI SYNTHESIS (schema_version 2)
// ==========================================
fun NetworkAiSynthesis.toDomain(): DomainAiSynthesis? {
    val syncData = this.synthesis ?: return null
    return DomainAiSynthesis(
        timestamp = timestamp ?: 0L,
        contentFlags = contentFlags ?: emptyList(),
        executive = syncData.executive?.toDomain() ?: return null,
        pillarScorecard = syncData.pillarScorecard?.map { it.toDomain() } ?: emptyList(),
        horizons = syncData.horizons?.toDomain() ?: DomainHorizons(null, null, null)
    )
}

fun NetworkExecutiveBlock.toDomain(): DomainExecutiveBlock {
    return DomainExecutiveBlock(
        headline = headline ?: "",
        alignmentWithMacro = AlignmentState.fromString(alignmentWithMacro),
        alignmentNote = alignmentNote ?: "",
        whatChanged = whatChanged ?: "",
        shifts = shifts?.map { it.toDomain() } ?: emptyList()
    )
}

fun NetworkShift.toDomain(): DomainShift {
    return DomainShift(
        metricId = metricId ?: "",
        direction = ShiftDirection.fromString(direction),
        note = note ?: ""
    )
}

fun NetworkPillarScorecardEntry.toDomain(): DomainPillarScorecardEntry {
    return DomainPillarScorecardEntry(
        pillar = IndicatorCategory.fromString(pillar),
        stance = SignalColor.fromString(stance),
        agreement = AgreementState.fromString(agreement),
        oneLiner = oneLiner ?: ""
    )
}

fun NetworkHorizons.toDomain(): DomainHorizons {
    return DomainHorizons(
        shortTerm = shortTerm?.toDomain(),
        mediumTerm = mediumTerm?.toDomain(),
        longTerm = longTerm?.toDomain()
    )
}

fun NetworkHorizonBlock.toDomain(): DomainHorizonBlock {
    return DomainHorizonBlock(
        posture = posture ?: "",
        timeWindow = timeWindow ?: "",
        riskLevel = RiskImpactLevel.fromString(riskLevel),
        whatThisMeans = whatThisMeans ?: "",
        watchFor = watchFor
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