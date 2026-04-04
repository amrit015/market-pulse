package com.marketlabs.pulse.ui.screens.riskRadar

import com.marketlabs.pulse.storage.model.riskRadar.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar

data class RiskRadarUiState(
    val isLoading: Boolean = false,
    val riskRadar: RiskRadar? = null,
    val tailRisks: MarketRiskAssessment? = null,
    val errorMessage: String? = null
)
