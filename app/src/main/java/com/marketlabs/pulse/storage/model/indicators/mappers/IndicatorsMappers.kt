package com.marketlabs.pulse.storage.model.indicators.mappers

import com.marketlabs.pulse.network.model.indicators.NetworkIndicatorItem
import com.marketlabs.pulse.network.model.indicators.NetworkMacroVitals
import com.marketlabs.pulse.network.model.indicators.NetworkMarketAction
import com.marketlabs.pulse.network.model.indicators.NetworkMarketPhase
import com.marketlabs.pulse.network.model.indicators.NetworkPhaseDetails
import com.marketlabs.pulse.network.model.indicators.NetworkVitalItem
import com.marketlabs.pulse.storage.database.entity.IndicatorsEntity
import com.marketlabs.pulse.storage.model.indicators.ActionMetric
import com.marketlabs.pulse.storage.model.indicators.DomainMacroVitals
import com.marketlabs.pulse.storage.model.indicators.DomainMarketAction
import com.marketlabs.pulse.storage.model.indicators.DomainMarketPhase
import com.marketlabs.pulse.storage.model.indicators.IndicatorItem
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.storage.model.indicators.PhaseDetails
import com.marketlabs.pulse.storage.model.indicators.VitalItem
import com.marketlabs.pulse.storage.model.indicators.enums.ActionSignal
import com.marketlabs.pulse.storage.model.indicators.enums.SetupPhase
import com.marketlabs.pulse.storage.model.indicators.enums.SignalColor
import com.marketlabs.pulse.storage.model.indicators.enums.VerdictCall

// ==========================================
// 🌐 NETWORK TO DOMAIN: MARKET PHASE
// ==========================================
fun NetworkMarketPhase.toDomain(): DomainMarketPhase {
    return DomainMarketPhase(
        timestamp = timestamp ?: 0L,
        marketRegime = marketRegime,
        setupPhase = SetupPhase.fromString(setupPhase),
        verdictScore = verdict?.score,
        previousScore = verdict?.previousScore,
        verdictCall = VerdictCall.fromString(verdict?.call),
        verdictAction = verdict?.action,
        verdictFormula = verdict?.formula,

        // Inject the root signal directly into the pillar details
        trendDetails = pillars?.trend?.toDomain(signals?.trend),
        healthDetails = pillars?.health?.toDomain(signals?.health),
        riskDetails = pillars?.risk?.toDomain(signals?.risk),
        valuationDetails = pillars?.valuation?.toDomain(signals?.valuation)
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
        fearAndGreed = ActionMetric(
            value = rawMetrics?.fearAndGreed?.value,
            change = rawMetrics?.fearAndGreed?.change,
            signalColor = SignalColor.fromString(rawMetrics?.fearAndGreed?.signalColor)
        ),
        putCallRatio = ActionMetric(
            value = rawMetrics?.putCallRatio?.value,
            change = rawMetrics?.putCallRatio?.change,
            signalColor = SignalColor.fromString(rawMetrics?.putCallRatio?.signalColor)
        ),
        sp500Rsi = ActionMetric(
            value = rawMetrics?.sp500Rsi?.value,
            change = rawMetrics?.sp500Rsi?.change,
            signalColor = SignalColor.fromString(rawMetrics?.sp500Rsi?.signalColor)
        ),
        smaExtension = ActionMetric(
            value = rawMetrics?.smaExtension?.value,
            change = rawMetrics?.smaExtension?.change,
            signalColor = SignalColor.fromString(rawMetrics?.smaExtension?.signalColor)
        )
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