package com.marketlabs.pulse.ui.compose.summary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.ui.viewmodels.MarketSummaryViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.ui.viewmodels.MarketSummaryUiState

/**
 * The Stateful Entry Point (The "Manager") for the Market Summary feature.
 *
 * Responsibilities:
 * 1. **State Management**: Collects [MarketSummaryUiState] from the [MarketSummaryViewModel].
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
    viewModel: MarketSummaryViewModel = hiltViewModel()
) {
    // 1. STATE COLLECTION
    // We use collectAsStateWithLifecycle() so flow collection stops when the app goes
    // to the background, saving battery and resources.
    val uiState by viewModel.summaryUiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    // 2. SIDE EFFECTS (Snackbars)
    // We need a host state to display Snackbars on top of the content.
    val snackbarHostState = remember { SnackbarHostState() }

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
            onRefresh = { viewModel.refreshData(force = true) }
        ) {

            // 5. CONTENT SWITCHING
            // Decide what to show based on the current UI State.
            when (val state = uiState) {

                // Case A: Initial Load / Empty DB
                // We show a loading spinner centered on the screen.
                is MarketSummaryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // Case B: Data Available
                // We have data (either fresh or cached). We delegate rendering to the stateless screen.
                is MarketSummaryUiState.Success -> {
                    MarketSummaryScreen(
                        data = state.data,
                        scaffoldPadding = scaffoldPadding
                    )
                }

                // Case C: Critical Failure
                // This state only happens if the DB is empty AND the Network failed.
                // We show a full-screen error with a Retry button.
                is MarketSummaryUiState.Error -> {
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