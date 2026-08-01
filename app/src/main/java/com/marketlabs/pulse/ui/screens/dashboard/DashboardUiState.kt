package com.marketlabs.pulse.ui.screens.dashboard

import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.dashboard.MarketState

data class DashboardUiState(
    val marketState: MarketState? = null,
    val assets: List<AssetOverview?> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)