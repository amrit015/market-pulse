package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.marketlabs.pulse.R

@Composable
fun TutorialsGaugesRoute(
    onNavigateUp: () -> Unit,
    onNavigateToArticle: (String) -> Unit = {},
    viewModel: TutorialsGaugesViewModel = hiltViewModel()
) {
    TutorialsGaugesScreen(
        title = stringResource(id = R.string.tutorials_glossary_screen_title, stringResource(id = viewModel.mechanism.titleRes)),
        uiState = viewModel.uiState,
        onNavigateUp = onNavigateUp,
        onNavigateToArticle = onNavigateToArticle
    )
}
