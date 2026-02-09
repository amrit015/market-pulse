package com.marketlabs.pulse.ui.compose.overview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.marketlabs.pulse.ui.viewmodels.OverviewViewModel

/**
 * Action: Replace DashBoardFragment with a stateful Compose route.
 */
@Composable
fun MarketOverviewRoute(
    scaffoldPadding: PaddingValues, // Passed from PulseNavGraph
    viewModel: OverviewViewModel = hiltViewModel()
) {

    MarketOverviewScreen(
        scaffoldPadding = scaffoldPadding,
        // data = uiState.data (Pass your chart data here)
    )
}