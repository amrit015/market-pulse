package com.marketlabs.pulse.ui.screens.indicators.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseErrorState
import com.marketlabs.pulse.ui.components.PulseLoadingIndicator
import com.marketlabs.pulse.ui.components.widgets.FavoriteStarToggle

/**
 * Stateful entry point for the pushed `metricDetail/{metricId}` destination -- every indicator card
 * tap navigates here.
 * Mirrors `AssetDetailRoute`'s plain `Scaffold`/`TopAppBar`/back-button shape (this screen
 * has no tabs or pinned rich header either).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricDetailRoute(
    scaffoldPadding: PaddingValues,
    onNavigateUp: () -> Unit,
    viewModel: MetricDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.metric?.name ?: uiState.metricId) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = stringResource(id = R.string.nav_back_content_description)
                        )
                    }
                },
                actions = {
                    FavoriteStarToggle(
                        isFavorite = uiState.isFavorite,
                        onClick = viewModel::toggleFavorite,
                        contentDescription = stringResource(
                            id = if (uiState.isFavorite) R.string.stock_analysis_remove_favorite_content_description
                            else R.string.stock_analysis_add_favorite_content_description,
                            uiState.metric?.name ?: uiState.metricId
                        ),
                        modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_large))
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { topBarPadding ->
        val metric = uiState.metric
        if (metric != null) {
            MetricDetailScreen(
                metric = metric,
                glossaryEntry = uiState.glossaryEntry,
                historyPoints = uiState.historyPoints,
                isHistoryLoading = uiState.isHistoryLoading,
                selectedChartRange = uiState.selectedChartRange,
                availableChartRanges = uiState.availableChartRanges,
                onRangeSelected = viewModel::onRangeSelected,
                scaffoldPadding = PaddingValues(
                    top = topBarPadding.calculateTopPadding(),
                    bottom = scaffoldPadding.calculateBottomPadding()
                ),
                modifier = Modifier.fillMaxSize()
            )
        } else if (uiState.hasTimedOut) {
            PulseErrorState(
                message = stringResource(id = R.string.metric_detail_not_found),
                onRetry = onNavigateUp,
                actionLabel = stringResource(id = R.string.action_go_back),
                modifier = Modifier.padding(top = topBarPadding.calculateTopPadding())
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topBarPadding.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                PulseLoadingIndicator()
            }
        }
    }
}
