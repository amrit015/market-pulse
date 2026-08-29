package com.marketlabs.pulse.ui.screens.insights

import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.positioning.DomainMarketPositioning
import com.marketlabs.pulse.storage.model.posture.DomainMarketPosture
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook

data class InsightsUiState(
    val isLoading: Boolean = false,
    val weeklyPlaybook: WeeklyPlaybook? = null,
    val tailRisks: MarketRiskAssessment? = null,
    val marketPosture: DomainMarketPosture? = null,
    val marketPositioning: DomainMarketPositioning? = null,
    // 💡 Drives the pinned PulseTabRow (Playbook/Risks/Posture/Positioning) added when this screen
    // moved from one long stacked scroll to per-section tabs -- same shape as StockDetailUiState's
    // identical field.
    val selectedTabIndex: Int = 0,
    // 💡 2026-08-27 interpretive-layer spec, Layer 3 -- default false (explainer shown) rather
    // than true, so a slow-to-load DataStore read never flashes the explainer on then off.
    val isPositioningIntroDismissed: Boolean = false,
    val isPostureIntroDismissed: Boolean = false,
    val errorMessage: String? = null
)