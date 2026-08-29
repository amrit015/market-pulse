package com.marketlabs.pulse.ui.screens.insights.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseTabRow
import com.marketlabs.pulse.ui.screens.insights.InsightsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsRoute(
    scaffoldPadding: PaddingValues,
    onNavigateToGlossaryDetail: (metricIds: List<String>, title: String, description: String?, status: String?) -> Unit,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullRefreshState = rememberPullToRefreshState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onStart()
                Lifecycle.Event.ON_STOP -> viewModel.onStop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = scaffoldPadding.calculateTopPadding())
    ) {
        // 💡 Pinned above the pull-to-refresh area, same shape as StockDetailRoute's pinned
        // DetailHeader + tab row -- the tabs stay reachable and in place regardless of which tab's
        // content is loading/erroring/showing below, and pulling down works from any tab.
        PulseTabRow(
            tabs = InsightsTab.entries.map { stringResource(id = it.labelRes) },
            selectedTabIndex = uiState.selectedTabIndex,
            onTabSelected = viewModel::onTabSelected
        )

        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refreshInsights(force = true) },
                state = pullRefreshState,
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = uiState.isLoading,
                        state = pullRefreshState
                    )
                }
            ) {
                // 💡 Checks all 4 tabs' data now, not just Playbook/Risks -- with tabs, a reader can
                // land directly on Posture or Positioning, so gating "is anything available" on only
                // the first two sections meant a run where only Posture/Positioning had loaded (both
                // Playbook and Risks null) fell through this `when` with nothing rendered at all.
                when {
                    // Initial Load
                    uiState.weeklyPlaybook == null && uiState.tailRisks == null &&
                        uiState.marketPosture == null && uiState.marketPositioning == null &&
                        uiState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    // Data Available
                    uiState.weeklyPlaybook != null || uiState.tailRisks != null ||
                        uiState.marketPosture != null || uiState.marketPositioning != null -> {
                        InsightsScreen(
                            uiState = uiState,
                            selectedTabIndex = uiState.selectedTabIndex,
                            scaffoldPadding = PaddingValues(bottom = scaffoldPadding.calculateBottomPadding()),
                            onNavigateToGlossaryDetail = onNavigateToGlossaryDetail,
                            onDismissPositioningIntro = viewModel::dismissPositioningIntro,
                            onDismissPostureIntro = viewModel::dismissPostureIntro
                        )
                    }

                    // Error
                    uiState.errorMessage != null -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${stringResource(id = R.string.error_prefix)} ${uiState.errorMessage}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(
                                onClick = { viewModel.refreshInsights(force = true) },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text(stringResource(id = R.string.action_retry))
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = scaffoldPadding.calculateBottomPadding())
            )
        }
    }
}