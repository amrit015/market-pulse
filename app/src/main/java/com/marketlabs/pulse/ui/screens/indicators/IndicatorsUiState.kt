package com.marketlabs.pulse.ui.screens.indicators

import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.ui.screens.indicators.views.IndicatorsTab

data class IndicatorsUiState(
    val data: MarketIndicators? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    /** Default lands on Momentum, not the first-listed Favorites tab -- see `IndicatorsTab`'s own doc comment. */
    val selectedTabIndex: Int = IndicatorsTab.TACTICAL_MOMENTUM.ordinal,
    /** Local-only, per-device -- same `FavoriteMetricsRepository` the Favorites tab's stars read. */
    val favoriteMetricIds: Set<String> = emptySet()
)