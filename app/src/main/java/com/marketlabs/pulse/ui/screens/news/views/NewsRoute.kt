package com.marketlabs.pulse.ui.screens.news.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.screens.news.NewsViewModel

/**
 * Added with Claude Code assistance: News is no longer a bottom-nav tab — it's reached only by
 * tapping the Dashboard's "Latest News" chevron or a specific preview card — so this now owns its
 * own `Scaffold`/`TopAppBar` with a back button, mirroring `PulseWebViewScreen`'s push-screen shape.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsRoute(
    scaffoldPadding: PaddingValues,
    onNavigateUp: () -> Unit, // Added with Claude Code assistance.
    viewModel: NewsViewModel = hiltViewModel(),
    onNavigateToWebView: (String) -> Unit, // webview navigation action
    // Added with Claude Code assistance: set when a Dashboard news preview card is tapped, so the
    // respective card can be scrolled into view + highlighted here.
    highlightedArticleUrl: String? = null,
    onHighlightConsumed: () -> Unit = {}
) {
    // 💡 NEW: Collect the single unified state
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Added with Claude Code assistance: this screen keeps its own local copy — deliberately NOT
    // keyed off `highlightedArticleUrl` — so the highlight border/scroll persists for the rest of
    // this visit even after the NavGraph's one-shot value is nulled out below. Re-fires only when
    // a genuinely new (non-null) value arrives, e.g. tapping a different preview card later.
    var localHighlightedArticleUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(highlightedArticleUrl) {
        if (highlightedArticleUrl != null) {
            localHighlightedArticleUrl = highlightedArticleUrl
            onHighlightConsumed()
        }
    }
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

    // Trigger Snackbar when a soft error is emitted (e.g., failed background refresh)
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.news_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = stringResource(id = R.string.nav_back_content_description)
                        )
                    }
                },
                // Added with Claude Code assistance: matches the screen's background so the
                // toolbar doesn't render as a visibly different-colored band above the content.
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { topBarPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topBarPadding.calculateTopPadding())
        ) {
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refreshNews(force = true) },
                state = pullRefreshState, // Pass the state
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = uiState.isLoading,
                        state = pullRefreshState
                    )
                }
            ) {
                when {
                    // Case A: Data Available (Takes top priority)
                    uiState.news != null -> {
                        NewsScreen(
                            data = uiState.news!!,
                            scaffoldPadding = scaffoldPadding,
                            onArticleClick = onNavigateToWebView,
                            highlightedArticleUrl = localHighlightedArticleUrl
                        )
                    }

                    // Case B: Initial Load / Empty DB
                    // (Compiler knows news is null if it reached here)
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    // Case C: Critical Failure
                    // (Compiler knows news is null AND isLoading is false if it reached here)
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
                                onClick = { viewModel.refreshNews(force = true) },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text(stringResource(id = R.string.action_retry))
                            }
                        }
                    }

                    // Case D: Completely Empty
                    // (No data, not loading, no error. It just gracefully falls into 'else')
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.news_empty_state),
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
}