package com.marketlabs.pulse.ui.screens.summary.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.ui.screens.summary.SummaryUiState
import com.marketlabs.pulse.ui.screens.summary.SummaryViewModel
import com.marketlabs.pulse.utils.enums.ReportType

/**
 * The Stateful Entry Point (The "Manager") for the Market Summary feature.
 *
 * Responsibilities:
 * 1. **State Management**: Collects [com.marketlabs.pulse.ui.screens.summary.SummaryUiState] from the [com.marketlabs.pulse.ui.screens.summary.SummaryViewModel].
 * 2. **Lifecycle Awareness**: Uses [collectAsStateWithLifecycle] to safely observe flows.
 * 3. **Side Effects**: Listens for one-time events (like Network Errors) to show error ui.
 * 4. **Scaffolding**: Sets up the global UI structure (Scaffold, Pull-to-Refresh).
 *
 * This component does NOT render the specific UI cards; it delegates that to [MarketSummaryScreen].
 *
 * @param viewModel The Hilt-injected ViewModel that holds the business logic.
 */
@OptIn(ExperimentalMaterial3Api::class) // Required for PullToRefreshBox
@Composable
fun MarketSummaryRoute(
    scaffoldPadding: PaddingValues,
    onNavigateToIndicators: () -> Unit,
    // 💡 Reports up to MainActivity so the global top bar can show the dynamic report-type label
    // ("Daily Update"/"Weekend Update") instead of a fixed "Summary" -- MainActivity stays
    // intentionally dumb about screen internals otherwise, so this one value is threaded up
    // through PulseNavGraph rather than the top bar reaching into this screen's ViewModel
    // directly. Same shape as onDriversNavigatedToIndicators's up-reporting.
    onReportTypeLoaded: (ReportType?) -> Unit = {},
    // spec-20260902-market-sentiment-android.md: Market Sentiment's whole-card tap target --
    // Posture is an Insights tab, not its own destination, so this lands on Insights at that
    // specific tab. Same pass-through shape as onNavigateToIndicators above.
    onNavigateToPosture: () -> Unit = {},
    viewModel: SummaryViewModel = hiltViewModel()
) {
    // 1. STATE COLLECTION
    // We use collectAsStateWithLifecycle() so flow collection stops when the app goes
    // to the background, saving battery and resources.
    val uiState by viewModel.summaryUiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    // 2. SIDE EFFECTS (Snackbars)
    // We need a host state to display Snackbars on top of the content.
    val snackbarHostState = remember { SnackbarHostState() }

    // Remember the state so we can pass it to the custom indicator
    val pullRefreshState = rememberPullToRefreshState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // 💡 NEW: Bind the ViewModel's SyncManager to the Android UI Lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onStart()
                Lifecycle.Event.ON_STOP -> viewModel.onStop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // This LaunchedEffect runs once when the component starts.
    // It listens to the 'errorEvents' Channel for "one-shot" messages (e.g., "No Internet").
    LaunchedEffect(viewModel.errorEvents) {
        viewModel.errorEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // 3. UI - Box and Snackbar
    // Action: Use Box instead of Scaffold to avoid redundant padding
    Box(modifier = Modifier.fillMaxSize()) {

        // 4. PULL-TO-REFRESH CONTAINER
        // Wraps the entire screen content. When the user swipes down,
        // it triggers viewModel.refreshData(force = true).
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshData(force = true) },
            state = pullRefreshState, // Pass the state
            indicator = {
                // Override the indicator to add top padding!
                Indicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        // Push the spinner down below the status bar
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
                    isRefreshing = isRefreshing,
                    state = pullRefreshState
                )
            }
        ) {

            // 5. CONTENT SWITCHING
            // Decide what to show based on the current UI State.
            when (val state = uiState) {

                // Case A: Initial Load / Empty DB
                // We show a loading spinner centered on the screen.
                is SummaryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // Case B: Data Available
                // We have data (either fresh or cached). We delegate rendering to the stateless screen.
                is SummaryUiState.Success -> {

                    LaunchedEffect(state.dataV3?.reportType) {
                        onReportTypeLoaded(state.dataV3?.reportType)
                    }

                    MarketSummaryScreen(
                        data = state.dataV3,
                        scaffoldPadding = scaffoldPadding,
                        onNavigateToIndicators = onNavigateToIndicators,
                        onNavigateToPosture = onNavigateToPosture
                    )
                }

                // Case C: Critical Failure
                // This state only happens if the DB is empty AND the Network failed.
                // We show a full-screen error with a Retry button.
                is SummaryUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.refreshData(force = true) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }

        // Manually place the SnackbarHost at the bottom of the Box
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // Use the scaffoldPadding to ensure it sits above the Bottom Navigation
                .padding(bottom = scaffoldPadding.calculateBottomPadding())
        )
    }
}