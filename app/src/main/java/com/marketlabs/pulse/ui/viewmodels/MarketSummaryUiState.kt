package com.marketlabs.pulse.ui.viewmodels

import com.marketlabs.pulse.storage.model.summary.MarketPulse

sealed interface MarketSummaryUiState {
    data object Loading : MarketSummaryUiState
    data class Success(val dataV3: MarketPulse?, val dataV2: MarketPulse?) : MarketSummaryUiState
    data class Error(val message: String) : MarketSummaryUiState // Only for "Empty DB + Network Fail"
}