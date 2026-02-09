package com.marketlabs.pulse.ui.viewmodels

import com.marketlabs.pulse.storage.model.summary.MarketPulse

sealed interface MarketSummaryUiState {
    data object Loading : MarketSummaryUiState
    data class Success(val data: MarketPulse) : MarketSummaryUiState
    data class Error(val message: String) : MarketSummaryUiState // Only for "Empty DB + Network Fail"
}