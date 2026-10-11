package com.marketlabs.pulse.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Stateful wrapper for [DataSyncScreen]. */
@Composable
fun DataSyncRoute(
    onNavigateUp: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    viewModel: DataSyncViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DataSyncScreen(
        uiState = uiState,
        onAnalyticsEnabledChange = viewModel::setAnalyticsEnabled,
        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
        onNavigateUp = onNavigateUp
    )
}
