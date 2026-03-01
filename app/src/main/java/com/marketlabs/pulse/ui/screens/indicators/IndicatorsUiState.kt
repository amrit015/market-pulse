package com.marketlabs.pulse.ui.screens.indicators

import com.marketlabs.pulse.storage.model.indicators.MarketIndicators

data class IndicatorsUiState(
    val data: MarketIndicators? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)