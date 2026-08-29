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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.marketlabs.pulse.R

/**
 * Stateful entry point for the pushed `glossaryDetail/{metricId}/{title}` destination -- mirrors
 * `MetricDetailRoute`'s plain `Scaffold`/`TopAppBar`/back-button shape, but with no loading state:
 * `GlossaryDetailViewModel.uiState` is a plain synchronous lookup, not a `StateFlow` fed by a
 * repository, so there's nothing to collect or wait on.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryDetailRoute(
    scaffoldPadding: PaddingValues,
    onNavigateUp: () -> Unit,
    viewModel: GlossaryDetailViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

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
            scaffoldPadding = PaddingValues(
                top = topBarPadding.calculateTopPadding(),
                bottom = scaffoldPadding.calculateBottomPadding()
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}
