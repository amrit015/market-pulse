package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseBackTitleRow
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.tutorials.TutorialsHubGrid
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Per-mechanism indicator list (the target of a mechanism deck's last card, "See all indicators")
 * -- stateless; `TutorialsGaugesRoute` resolves `uiState` from `IndicatorArticlesProvider` via the
 * ViewModel. Each gauge is now a [TutorialsHubGrid] tile (title + one-line subtitle, same tile the
 * Learn hub uses elsewhere) that pushes the gauge's own full `indicator_articles.json` article via
 * [onNavigateToArticle] -- this screen used to render a short `metric_glossary.json` blurb inline
 * with nowhere further to tap; it's now a directory into the fuller articles instead. The intro
 * card above the grid is per-category ([TutorialsGaugeCategoryUi.introRes]), not one fixed string
 * repeated for every pillar, so it can name that pillar's own gauges rather than generic examples.
 * No `Scaffold`/`TopAppBar`, same as every other screen reached from Settings (see
 * `DocumentSectionsScreen`'s doc comment).
 */
@Composable
fun TutorialsGaugesScreen(
    title: String,
    uiState: TutorialsGaugesUiState,
    onNavigateUp: () -> Unit,
    onNavigateToArticle: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        PulseBackTitleRow(title = title, onNavigateUp = onNavigateUp)
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            uiState.categories.forEach { category ->
                // 💡 PulseCard(DATA) -- this app's card system, matching the "one card per intro"
                // shape every other glossary/reference list in the app uses.
                PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = category.introRes),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
                    )
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))

                // One mechanism per screen now, so the category heading would just repeat the title.
                if (uiState.categories.size > 1) {
                    Text(
                        text = stringResource(id = category.titleRes),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LocalPulseColors.current.accentPrimary
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                }

                TutorialsHubGrid(
                    items = category.gauges,
                    title = { stringResource(id = it.titleRes) },
                    subtitle = { it.subtitle },
                    onClick = { onNavigateToArticle(it.metricId) }
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))
            }
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val previewUiState = TutorialsGaugesUiState(
    categories = listOf(
        TutorialsGaugeCategoryUi(
            titleRes = R.string.tutorials_gauges_category_tactical_momentum,
            introRes = R.string.tutorials_gauges_intro_tactical_momentum,
            gauges = listOf(
                TutorialsGaugeUi(
                    metricId = "fear_and_greed",
                    titleRes = R.string.tutorials_gauge_title_fear_and_greed,
                    subtitle = "CNN's composite mood gauge — 0 to 100, from Extreme Fear to Extreme Greed."
                )
            )
        )
    )
)

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewTutorialsGaugesScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        TutorialsGaugesScreen(title = "Tactical Momentum indicators", uiState = previewUiState, onNavigateUp = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewTutorialsGaugesScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        TutorialsGaugesScreen(title = "Tactical Momentum indicators", uiState = previewUiState, onNavigateUp = {})
    }
}
