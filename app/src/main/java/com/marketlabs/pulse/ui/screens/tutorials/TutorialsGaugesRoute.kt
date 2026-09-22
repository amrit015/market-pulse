package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.tutorials.Mechanism

@Composable
fun TutorialsGaugesRoute(
    onNavigateUp: () -> Unit,
    viewModel: TutorialsGaugesViewModel = hiltViewModel()
) {
    TutorialsGaugesScreen(
        title = if (viewModel.mechanisms.size > 1) {
            stringResource(id = R.string.tutorials_glossary_all_title)
        } else {
            stringResource(id = R.string.tutorials_glossary_screen_title, stringResource(id = viewModel.mechanisms.first().titleRes))
        },
        uiState = viewModel.uiState,
        onNavigateUp = onNavigateUp
    )
}
