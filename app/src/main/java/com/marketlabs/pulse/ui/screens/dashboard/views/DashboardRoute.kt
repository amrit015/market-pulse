package com.marketlabs.pulse.ui.screens.dashboard.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.screens.dashboard.DashboardViewModel
import com.marketlabs.pulse.ui.components.PulseErrorState
import com.marketlabs.pulse.ui.components.PulseLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardRoute(
    scaffoldPadding: PaddingValues,
    onNavigateToNews: () -> Unit, // Added with Claude Code assistance.
    onNavigateToNewsArticle: (String) -> Unit, // Added with Claude Code assistance.
    onNavigateToAssetDetail: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val latestNewsPreview by viewModel.latestNewsPreview.collectAsStateWithLifecycle() // Added with Claude Code assistance.
    val snackbarHostState = remember { SnackbarHostState() }
    val pullRefreshState = rememberPullToRefreshState()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.onStart()
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.onStop()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            if (uiState.assets.isNotEmpty()) {
                // Cache already on screen -- a background refresh failure is a soft error, surfaced
                // as a toast rather than replacing content that's still good to show.
                snackbarHostState.showSnackbar(message)
                viewModel.clearError()
            }
            // No cache at all: leave errorMessage set so the persistent error state below stays up
            // until the user retries (fetchDashboard clears it itself on the next attempt).
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshDashboard() },
            state = pullRefreshState,
            indicator = {
                // 💡 Brand-consistent replacement for Material's default spinner -- same
                // PulseLoadingIndicator the splash screen and this screen's own isLoading state
                // below use, shrunk down via its `size` param. `distanceFraction` (0 at rest, 1 at
                // the release threshold, PullToRefreshState's own public API) drives fade/scale-in
                // while dragging; once isRefreshing is true it's pinned fully visible regardless of
                // finger position.
                val indicatorVisibleFraction = if (uiState.isRefreshing) {
                    1f
                } else {
                    pullRefreshState.distanceFraction.coerceIn(0f, 1f)
                }
                if (indicatorVisibleFraction > 0f) {
                    PulseLoadingIndicator(
                        size = 40.dp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                            .alpha(indicatorVisibleFraction)
                            .scale(indicatorVisibleFraction)
                    )
                }
            }
            ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PulseLoadingIndicator()
                    }
                }
                uiState.assets.isNotEmpty() -> {
                    DashboardScreen(
                        marketState = uiState.marketState,
                        assets = uiState.assets,
                        scaffoldPadding = scaffoldPadding,
                        newsArticles = latestNewsPreview,
                        onNewsArticleClick = onNavigateToNewsArticle,
                        onNavigateToNews = onNavigateToNews,
                        onAssetClick = onNavigateToAssetDetail,
                        getIntradayStream = viewModel::getIntradayStream
                    )
                }
                uiState.errorMessage != null -> {
                    PulseErrorState(
                        message = uiState.errorMessage!!,
                        onRetry = { viewModel.refreshDashboard() }
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.dashboard_empty_state),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
