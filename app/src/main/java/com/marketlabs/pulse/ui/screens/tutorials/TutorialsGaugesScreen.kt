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
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Per-mechanism indicator list (the target of a mechanism deck's last card) -- stateless; `TutorialsGaugesRoute` resolves
 * `uiState` from `MetricGlossaryProvider` via the ViewModel. Definitions render exactly as bundled
 * (`what_it_is` only, not the fuller what-it-is/how-to-read/bands/gotchas shape the real per-metric
 * detail pages show) -- this is a browse/reference list, not a replacement for tapping into a gauge's
 * own detail page for the full breakdown. No `Scaffold`/`TopAppBar`, same as every other screen
 * reached from Settings (see `DocumentSectionsScreen`'s doc comment).
 */
@Composable
fun TutorialsGaugesScreen(
    title: String,
    uiState: TutorialsGaugesUiState,
    onNavigateUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        PulseBackTitleRow(title = title, onNavigateUp = onNavigateUp)
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            // 💡 PulseCard(DATA) -- this app's card system, matching the "one card per list entry"
            // shape every other glossary/reference list in the app uses.
            PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.tutorials_gauges_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))

            uiState.categories.forEach { category ->
                // One mechanism per screen now, so the category heading would just repeat the title.
                if (uiState.categories.size > 1) {
                    Text(
                        text = stringResource(id = category.titleRes),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LocalPulseColors.current.accentPrimary
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                }

                category.gauges.forEach { gauge ->
                    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                            Text(
                                text = stringResource(id = gauge.titleRes),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = gauge.whatItIs,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LocalPulseColors.current.onSurfaceMuted,
                                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_tiny))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))
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
            gauges = listOf(
                TutorialsGaugeUi(
                    titleRes = R.string.tutorials_gauge_title_fear_and_greed,
                    whatItIs = "CNN's blend of 7 indicators into a single 0-100 score of immediate market psychology."
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
