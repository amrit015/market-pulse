package com.marketlabs.pulse.ui.screens.insights

import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook

data class InsightsUiState(
    val isLoading: Boolean = false,
    val weeklyPlaybook: WeeklyPlaybook? = null,
    val tailRisks: MarketRiskAssessment? = null,
    val errorMessage: String? = null
)