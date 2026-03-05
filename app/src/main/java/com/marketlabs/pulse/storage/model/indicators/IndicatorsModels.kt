package com.marketlabs.pulse.storage.model.indicators

import com.marketlabs.pulse.storage.model.indicators.enums.SignalColor
import com.marketlabs.pulse.storage.model.indicators.enums.VerdictCall

data class MarketIndicators(
    val dateId: String,
    val lastSyncedTimestamp: Long,
    val lastUpdated: Long,
    val summary: PhaseSummary?,
    val trendPhase: PhaseDetails?,
    val healthPhase: PhaseDetails?,
    val riskPhase: PhaseDetails?
)

data class PhaseSummary(
    val score: Int?,
    val call: VerdictCall?,
    val action: String?,
    val marketRegime: String?,
    val setupPhase: String?,
    val trendSignal: String?,
    val healthSignal: String?,
    val riskSignal: String?
)

data class PhaseDetails(
    val phaseName: String?,
    val summary: String?,
    val indicators: List<IndicatorItem>?
)

data class IndicatorItem(
    val name: String?,
    val value: String?,
    val changePercent: String?,
    val signal: String?,
    val signalColor: SignalColor?, // 💡 Mapped to Enum for easy UI coloring
    val description: String?
)