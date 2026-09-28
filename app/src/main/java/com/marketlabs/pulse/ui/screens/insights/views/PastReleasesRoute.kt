package com.marketlabs.pulse.ui.screens.insights.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.marketlabs.pulse.ui.components.PulseErrorState
import com.marketlabs.pulse.ui.components.PulseLoadingIndicator
import com.marketlabs.pulse.ui.screens.insights.InsightsViewModel

/**
 * Stateful -- pushed destination (see PulseRoutes.PAST_RELEASES), not a bottom-nav tab, so it owns
 * its own `Scaffold`/`TopAppBar` with a back button, same pattern as IndicatorHorizonsRoute.kt/
 * SettingsScreen.kt/NewsRoute.kt. Reuses `InsightsViewModel` rather than a dedicated ViewModel --
 * this screen needs nothing beyond the same `PastReleases` the Events tab already loaded (no nav
 * arguments, no distinct fetch), and `hiltViewModel()` here resolves to a fresh instance scoped to
 * this back stack entry that immediately re-collects the same Room-cached `Flow` the tab's own
 * instance reads, so no data is refetched or duplicated in flight.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastReleasesRoute(
    onNavigateUp: () -> Unit,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pastReleases = uiState.pastReleases

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.section_past_releases),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (!pastReleases?.releases.isNullOrEmpty()) {
            PastReleasesScreen(
                pastReleases = pastReleases!!,
                innerPadding = innerPadding
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> PulseLoadingIndicator()
                    uiState.errorMessage != null -> {
                        PulseErrorState(
                            message = uiState.errorMessage!!,
                            onRetry = { viewModel.refreshInsights() }
                        )
                    }
                    else -> {
                        Text(
                            text = stringResource(id = R.string.past_releases_unavailable),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
