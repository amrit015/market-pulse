package com.marketlabs.pulse.storage.model.indicators

import com.marketlabs.pulse.storage.model.indicators.enums.*

// ============================================================================
// THE MASTER DOMAIN OBJECT
// ============================================================================
data class MarketIndicators(
    val dateId: String,
    val lastSyncedTimestamp: Long,

    val marketPhase: DomainMarketPhase?,
    val macroVitals: DomainMacroVitals?,
    val marketAction: DomainMarketAction?
)

// ============================================================================
// 🚦 PILLAR 1: MARKET PHASE
// ============================================================================
data class PhaseDetails(
    val overallSignal: String?, 
    val summary: String?,
    val indicators: List<IndicatorItem>
)

data class IndicatorItem(
    val name: String,
    val value: String?,
    val changePercent: String?,
    val signal: String?, 
    val signalColor: SignalColor,
    val description: String
)

// 💡 NEW: We also need to add the formula to DomainMarketPhase!
data class DomainMarketPhase(
    val timestamp: Long,
    val marketRegime: MarketRegime,
    val setupPhase: SetupPhase,
    val verdictScore: Int?,
    val previousScore: Int?,
    val verdictCall: VerdictCall,
    val verdictAction: String?,
    val verdictFormula: String?, // 💡 Added to show the calculation!
    val trendDetails: PhaseDetails?,
    val healthDetails: PhaseDetails?,
    val riskDetails: PhaseDetails?
)

// ============================================================================
// 🏥 PILLAR 2: MACRO VITALS
// ============================================================================
data class DomainMacroVitals(
    val timestamp: Long,
    val inflation: List<VitalItem>,
    val labor: List<VitalItem>,
    val growth: List<VitalItem>,
    val policy: List<VitalItem>
)

data class VitalItem(
    val id: String,
    val name: String,
    val displayValue: String?,
    val changeString: String?,
    val signalColor: SignalColor,
    val observationDate: String?
)

// ============================================================================
// 🎯 PILLAR 3: MARKET ACTION
// ============================================================================
data class DomainMarketAction(
    val timestamp: Long,
    val actionScore: Int?,
    val previousScore: Int?,
    val scoreChange: Int?,
    val signal: ActionSignal, 
    val colorString: SignalColor, 
    val description: String?,

    val fearAndGreed: ActionMetric,
    val putCallRatio: ActionMetric,
    val sp500Rsi: ActionMetric,
    val smaExtension: ActionMetric
)

data class ActionMetric(
    val value: String?,
    val signalColor: SignalColor
)