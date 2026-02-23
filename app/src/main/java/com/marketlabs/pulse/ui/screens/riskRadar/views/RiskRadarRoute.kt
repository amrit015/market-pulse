package com.marketlabs.pulse.ui.screens.riskRadar.views

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.screens.riskRadar.RiskRadarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskRadarRoute(
    scaffoldPadding: PaddingValues,
    viewModel: RiskRadarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    // Remember the state so we can pass it to the custom indicator
    val pullRefreshState = rememberPullToRefreshState()

    // Trigger Snackbar when an error is emitted
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refreshRisk(force = true) },
            state = pullRefreshState, // Pass the state
            indicator = {
                // Override the indicator to add top padding!
                Indicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        // Push the spinner down below the status bar
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
                    isRefreshing = uiState.isLoading,
                    state = pullRefreshState
                )
            }
        ) {
            when {
                // Case A: Initial Load / Empty DB
                uiState.riskRadar == null && uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // Case B: Data Available
                uiState.riskRadar != null -> {
                    RiskRadarScreen(
                        data = uiState.riskRadar!!,
                        scaffoldPadding = scaffoldPadding
                    )
                }

                // Case C: Critical Failure (No data + Error)
                uiState.riskRadar == null && uiState.errorMessage != null -> {
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
                            onClick = { viewModel.refreshRisk(force = true) },
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