package com.marketlabs.pulse.storage.model.indicators.mappers

import com.marketlabs.pulse.network.model.indicators.*
import com.marketlabs.pulse.storage.database.entity.IndicatorsEntity
import com.marketlabs.pulse.storage.model.indicators.*
import com.marketlabs.pulse.storage.model.indicators.enums.*

// ==========================================
// 🌐 NETWORK TO DOMAIN: MARKET PHASE
// ==========================================
fun NetworkMarketPhase.toDomain(): DomainMarketPhase {
    return DomainMarketPhase(
        timestamp = timestamp ?: 0L,
        marketRegime = MarketRegime.fromString(marketRegime),
        setupPhase = SetupPhase.fromString(setupPhase),
        verdictScore = verdict?.score,
        previousScore = verdict?.previousScore,
        verdictCall = VerdictCall.fromString(verdict?.call),
        verdictAction = verdict?.action,
        verdictFormula = verdict?.formula,

        // Inject the root signal directly into the pillar details
        trendDetails = pillars?.trend?.toDomain(signals?.trend),
        healthDetails = pillars?.health?.toDomain(signals?.health),
        riskDetails = pillars?.risk?.toDomain(signals?.risk)
    )
}

fun NetworkPhaseDetails.toDomain(overallSignal: String? = null): PhaseDetails {
    return PhaseDetails(
        overallSignal = overallSignal,
        summary = summary,
        // Map null lists to emptyList() to prevent UI crashes
        indicators = indicators?.map { it.toDomain() } ?: emptyList()
    )
}

fun NetworkIndicatorItem.toDomain(): IndicatorItem {
    return IndicatorItem(
        name = name ?: "Unknown",
        value = value,
        changePercent = changePercent,
        signal = signal,
        signalColor = SignalColor.fromString(signalColor),
        description = description ?: ""
    )
}

// ==========================================
// 🌐 NETWORK TO DOMAIN: MACRO VITALS
// ==========================================
fun NetworkMacroVitals.toDomain(): DomainMacroVitals {
    return DomainMacroVitals(
        timestamp = timestamp ?: 0L,
        inflation = metrics?.inflation?.map { it.toDomain() } ?: emptyList(),
        labor = metrics?.labor?.map { it.toDomain() } ?: emptyList(),
        growth = metrics?.growth?.map { it.toDomain() } ?: emptyList(),
        policy = metrics?.policy?.map { it.toDomain() } ?: emptyList()
    )
}

fun NetworkVitalItem.toDomain(): VitalItem {
    return VitalItem(
        id = id ?: "",
        name = name ?: "Unknown",
        displayValue = displayValue,
        changeString = change,
        signalColor = SignalColor.fromString(signalColor),
        observationDate = date
    )
}

// ==========================================
// 🌐 NETWORK TO DOMAIN: MARKET ACTION
// ==========================================
fun NetworkMarketAction.toDomain(): DomainMarketAction {
    return DomainMarketAction(
        timestamp = timestamp ?: 0L,
        actionScore = actionZone?.score,
        previousScore = actionZone?.previousScore,
        scoreChange = actionZone?.scoreChange,
        signal = ActionSignal.fromString(actionZone?.signal),
        colorString = SignalColor.fromString(actionZone?.color),
        description = actionZone?.description,
        fearAndGreed = ActionMetric(rawMetrics?.fearAndGreed?.value, SignalColor.fromString(rawMetrics?.fearAndGreed?.signalColor)),
        putCallRatio = ActionMetric(rawMetrics?.putCallRatio?.value, SignalColor.fromString(rawMetrics?.putCallRatio?.signalColor)),
        sp500Rsi = ActionMetric(rawMetrics?.sp500Rsi?.value, SignalColor.fromString(rawMetrics?.sp500Rsi?.signalColor)),
        smaExtension = ActionMetric(rawMetrics?.smaExtension?.value, SignalColor.fromString(rawMetrics?.smaExtension?.signalColor))
    )
}

// ==========================================
// 💾 DOMAIN TO ENTITY (Room Database Write)
// ==========================================
fun MarketIndicators.toEntity(): IndicatorsEntity {
    return IndicatorsEntity(
        dateId = dateId,
        lastSyncedTimestamp = lastSyncedTimestamp,
        marketPhase = marketPhase,
        macroVitals = macroVitals,
        marketAction = marketAction
    )
}

// ==========================================
// 💾 ENTITY TO DOMAIN (Room Database Read)
// ==========================================
fun IndicatorsEntity.toDomain(): MarketIndicators {
    return MarketIndicators(
        dateId = dateId,
        lastSyncedTimestamp = lastSyncedTimestamp,
        marketPhase = marketPhase,
        macroVitals = macroVitals,
        marketAction = marketAction
    )
}