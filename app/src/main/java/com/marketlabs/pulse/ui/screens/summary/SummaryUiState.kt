package com.marketlabs.pulse.ui.screens.summary

import com.marketlabs.pulse.storage.model.summary.MarketPulse

sealed interface SummaryUiState {
    data object Loading : SummaryUiState
    data class Success(val dataV3: MarketPulse?, val dataV2: MarketPulse?) : SummaryUiState
    data class Error(val message: String) : SummaryUiState // Only for "Empty DB + Network Fail"
}