package com.marketlabs.pulse.ui.screens.extra.charts.views

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.marketlabs.pulse.ui.screens.extra.charts.ChartsViewModel

/**
 * Action: Replace DashBoardFragment with a stateful Compose route.
 */
@Composable
fun MarketOverviewRoute(
    scaffoldPadding: PaddingValues, // Passed from PulseNavGraph
    viewModel: ChartsViewModel = hiltViewModel()
) {

    MarketOverviewScreen(
        scaffoldPadding = scaffoldPadding,
        // data = uiState.data (Pass your chart data here)
    )
}