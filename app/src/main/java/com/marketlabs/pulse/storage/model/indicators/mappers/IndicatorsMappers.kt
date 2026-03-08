package com.marketlabs.pulse.storage.model.indicators.mappers

import com.marketlabs.pulse.network.model.indicators.NetworkIndicatorItem
import com.marketlabs.pulse.network.model.indicators.NetworkPhaseDetails
import com.marketlabs.pulse.network.model.indicators.NetworkPhaseSummary
import com.marketlabs.pulse.storage.database.entity.IndicatorsEntity
import com.marketlabs.pulse.storage.model.indicators.IndicatorItem
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.storage.model.indicators.PhaseDetails
import com.marketlabs.pulse.storage.model.indicators.PhaseSummary
import com.marketlabs.pulse.storage.model.indicators.enums.SignalColor
import com.marketlabs.pulse.storage.model.indicators.enums.VerdictCall

// ==========================================
// 🌐 NETWORK TO DOMAIN
// ==========================================
fun NetworkPhaseSummary.toDomain(): PhaseSummary {
    return PhaseSummary(
        previousScore = verdict?.previousScore,
        score = verdict?.score,
        call = VerdictCall.fromString(verdict?.call),
        action = verdict?.action,
        marketRegime = marketRegime,
        setupPhase = setupPhase,
        trendSignal = signals?.trend,
        healthSignal = signals?.health,
        riskSignal = signals?.risk
    )
}

fun NetworkPhaseDetails.toDomain(): PhaseDetails {
    return PhaseDetails(
        phaseName = phaseName,
        summary = summary,
        indicators = indicators?.map { it.toDomain() }
    )
}

fun NetworkIndicatorItem.toDomain(): IndicatorItem {
    return IndicatorItem(
        name = name,
        value = value,
        changePercent = changePercent,
        signal = signal,
        signalColor = SignalColor.fromString(signalColor),
        description = description
    )
}

// ==========================================
// 💾 DOMAIN TO ENTITY (Room)
// ==========================================
fun MarketIndicators.toEntity(): IndicatorsEntity {
    return IndicatorsEntity(
        dateId = dateId,
        lastSyncedTimestamp = lastSyncedTimestamp,
        lastUpdated = lastUpdated,
        summary = summary,
        trendPhase = trendPhase,
        healthPhase = healthPhase,
        riskPhase = riskPhase
    )
}

// ==========================================
// 💾 ENTITY TO DOMAIN (Room)
// ==========================================
fun IndicatorsEntity.toDomain(): MarketIndicators {
    return MarketIndicators(
        dateId = dateId,
        lastSyncedTimestamp = lastSyncedTimestamp,
        lastUpdated = lastUpdated,
        summary = summary,
        trendPhase = trendPhase,
        healthPhase = healthPhase,
        riskPhase = riskPhase
    )
}