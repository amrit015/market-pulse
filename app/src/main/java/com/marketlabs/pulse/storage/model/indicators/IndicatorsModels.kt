package com.marketlabs.pulse.storage.model.indicators

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
// 🧠 AI SYNTHESIS PILLAR
// ==========================================
data class DomainAiSynthesis(
    val timestamp: Long,
    val overarchingCondition: String,
    val whatChanged: String,
    val pillarGlances: DomainPillarGlances?,
    val shortTerm: DomainHorizon?,
    val mediumTerm: DomainHorizon?,
    val longTerm: DomainHorizon?
)

data class DomainPillarGlances(
    val tactical: String,
    val systemicRisk: String,
    val valuation: String,
    val macro: String
)

data class DomainHorizon(
    val briefing: String,
    val riskLevel: String,
    val keyDriver: String,
    val whatToDo: String
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