package com.marketlabs.pulse.ui.screens.insights.glossary

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.R

/**
 * Stateful entry point for the pushed `glossaryDetail/{title}/{metricIds}/{chartMetricId}/{description}/{status}`
 * destination -- mirrors `MetricDetailRoute`'s plain `Scaffold`/`TopAppBar`/back-button shape,
 * including its `DisposableEffect` lifecycle wiring now that `GlossaryDetailViewModel.uiState` is a
 * proper `StateFlow` fed by an async history fetch (the chart), not the plain synchronous glossary
 * lookup this page used to be alone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryDetailRoute(
    scaffoldPadding: PaddingValues,
    onNavigateUp: () -> Unit,
    viewModel: GlossaryDetailViewModel = hiltViewModel()
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
                title = { Text(text = uiState.title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = stringResource(id = R.string.nav_back_content_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { topBarPadding ->
        GlossaryDetailScreen(
            title = uiState.title,
            description = uiState.description,
            sections = uiState.sections,
            mergedBands = uiState.mergedBands,
            currentBandIndex = uiState.currentBandIndex,
            chartMetricId = uiState.chartMetricId,
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
    }
}
