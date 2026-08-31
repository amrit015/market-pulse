package com.marketlabs.pulse.ui.screens.indicators.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.screens.indicators.IndicatorsViewModel

/**
 * Stateful -- pushed destination (see PulseRoutes.INDICATOR_HORIZONS), not a bottom-nav tab, so it
 * owns its own `Scaffold`/`TopAppBar` with a back button, same pattern as SettingsScreen.kt/
 * NewsRoute.kt. Reuses `IndicatorsViewModel` rather than a dedicated ViewModel -- this screen needs
 * nothing beyond the same `MarketIndicators` the Indicators tab already loaded (no nav arguments,
 * no distinct fetch), and `hiltViewModel()` here resolves to a fresh instance scoped to this back
 * stack entry that immediately re-collects the same Room-cached `Flow` the tab's own instance reads,
 * so no data is refetched or duplicated in flight.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndicatorHorizonsRoute(
    onNavigateUp: () -> Unit,
    viewModel: IndicatorsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val data = uiState.data

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.indicators_horizons_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = stringResource(id = R.string.nav_back_content_description)
                        )
                    }
                },
                // 💡 Matches the screen's background (and the same override every other pushed
                // destination's TopAppBar uses -- see NewsRoute.kt/AssetDetailRoute.kt) so the bar
                // doesn't render as a visibly different-colored band above the content. Left on the
                // default `colorScheme.surface` before, which reads noticeably different from
                // `colorScheme.background` now that light mode's background carries a per-preset
                // accent tint (`MarketPulseTheme.kt`'s `toColorScheme()`) that `surface` doesn't.
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (data?.aiSynthesis != null) {
            IndicatorHorizonsScreen(
                horizons = data.aiSynthesis.horizons,
                innerPadding = innerPadding
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = stringResource(id = R.string.indicators_horizons_unavailable),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
