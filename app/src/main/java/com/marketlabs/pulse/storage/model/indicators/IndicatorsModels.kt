package com.marketlabs.pulse.storage.model.indicators

import com.marketlabs.pulse.utils.enums.AgreementState
import com.marketlabs.pulse.utils.enums.AlignmentState
import com.marketlabs.pulse.utils.enums.IndicatorCategory
import com.marketlabs.pulse.utils.enums.RiskImpactLevel
import com.marketlabs.pulse.utils.enums.ShiftDirection
import com.marketlabs.pulse.utils.enums.SignalColor
import com.marketlabs.pulse.utils.enums.SubcategoryEnums

// ============================================================================
// THE MASTER DOMAIN OBJECT
// ============================================================================
data class MarketIndicators(
    val dateId: String,
    val lastSyncedTimestamp: Long,

    val aiSynthesis: DomainAiSynthesis?,
    val tacticalMomentum: DomainIndicatorPillar?,
    val systemicRisk: DomainIndicatorPillar?,
    val valuation: DomainIndicatorPillar?,
    val macroVitals: DomainIndicatorPillar?
)

// ==========================================
// 🧠 AI SYNTHESIS PILLAR (schema_version 2)
// ==========================================
data class DomainAiSynthesis(
    val timestamp: Long,
    val contentFlags: List<String>,
    val executive: DomainExecutiveBlock,
    val pillarScorecard: List<DomainPillarScorecardEntry>,
    val horizons: DomainHorizons
)

data class DomainExecutiveBlock(
    val headline: String,
    val alignmentWithMacro: AlignmentState,
    val alignmentNote: String,
    val whatChanged: String,
    val shifts: List<DomainShift>
)

data class DomainShift(
    val metricId: String,
    val direction: ShiftDirection,
    val note: String
)

// 💡 `pillar` reuses the existing app-wide `IndicatorCategory` enum (Summary domain's driver
// pillar tag) rather than a second identically-shaped enum -- both name the same 4 quantitative
// pillars. `contributingMetricIds` removed 2026-08-22 -- the backend dropped it from assembly;
// it was never rendered on this side either.
data class DomainPillarScorecardEntry(
    val pillar: IndicatorCategory,
    val stance: SignalColor,
    val agreement: AgreementState,
    val oneLiner: String
)

data class DomainHorizons(
    val shortTerm: DomainHorizonBlock?,
    val mediumTerm: DomainHorizonBlock?,
    val longTerm: DomainHorizonBlock?
)

// 💡 `riskLevel` reuses `RiskImpactLevel` (utils/enums/RiskEnums.kt) -- its `fromString` already
// normalizes "MODERATE" to `MEDIUM`, and the backend's horizon risk_level enum (LOW/MODERATE/HIGH)
// is a strict subset of what `RiskImpactLevel` already models, so no new risk enum is needed.
// `keyDrivers`/`DomainKeyDriver` removed 2026-08-22 -- the backend dropped `key_drivers[]` from
// the schema, prompt, validation, and assembly entirely.
data class DomainHorizonBlock(
    val posture: String,
    val timeWindow: String,
    val riskLevel: RiskImpactLevel,
    val whatThisMeans: String,
    val watchFor: String?
)

// ==========================================
// 📊 UNIFIED QUANTITATIVE PILLAR
// ==========================================
data class DomainIndicatorPillar(
    val timestamp: Long,
    val masterGauge: DomainMasterGauge?,
    val metrics: List<DomainUnifiedMetric>
)

data class DomainMasterGauge(
    val score: Int?,
    val previousScore: Int?,
    val scoreChange: Int?,
    val signalText: String?,
    val signalColor: SignalColor,
    val trendChangeLabel: String?
)

data class DomainUnifiedMetric(
    val id: String,
    val name: String,
    val category: String,
    val subcategory: SubcategoryEnums?,
    val valueRaw: Double?,
    val valueDisplay: String?,
    val previousValueRaw: Double?,
    val previousValueDisplay: String?,
    val changeRaw: Double?,
    val changeDisplay: String?,
    val signalText: String?,
    val signalColor: SignalColor,
    val releaseDate: String?
)