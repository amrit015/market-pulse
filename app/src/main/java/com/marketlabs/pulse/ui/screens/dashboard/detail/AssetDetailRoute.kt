package com.marketlabs.pulse.ui.screens.dashboard.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
 * Stateful entry point for the pushed `assetDetail/{symbol}` destination -- replaces
 * `AssetDetailBottomSheet`, so every dashboard tile tap (indices/sectors/crypto/commodities/VIX/
 * sentiment) now navigates here instead of opening a sheet. Mirrors `NewsRoute`'s plain
 * `Scaffold`/`TopAppBar`/back-button shape (this screen has no tabs or pinned rich header, unlike
 * `StockDetailRoute`, so a Material `TopAppBar` is enough).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailRoute(
    scaffoldPadding: PaddingValues,
    onNavigateUp: () -> Unit,
    viewModel: AssetDetailViewModel = hiltViewModel()
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
                title = { Text(text = uiState.asset?.name ?: uiState.symbol) },
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
        val asset = uiState.asset
        if (asset != null) {
            AssetDetailScreen(
                asset = asset,
                chartSeries = uiState.chartSeries,
                selectedChartRange = uiState.selectedChartRange,
                isChartLoading = uiState.isChartLoading,
                intradaySeries = uiState.intradaySeries,
                availableChartRanges = uiState.availableChartRanges,
                onChartRangeSelected = viewModel::selectChartRange,
                scaffoldPadding = PaddingValues(
                    top = topBarPadding.calculateTopPadding(),
                    bottom = scaffoldPadding.calculateBottomPadding()
                ),
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topBarPadding.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
