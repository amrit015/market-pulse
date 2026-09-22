package com.marketlabs.pulse.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors

/**
 * Shared renderer for any screen that's a flat list of heading+body sections — Terms & Conditions
 * and Privacy Policy, and the Tutorials hub's static articles. Per-section string resources at
 * each call site (each section its own resource, easy to diff/update independently; no precedent
 * in this codebase for one giant multi-paragraph string) — this composable just lays them out.
 *
 * A section with a blank heading renders body-only (no heading row) — used for flowing-paragraph
 * content that doesn't have a natural per-paragraph heading, so callers don't need a second shape
 * just for that case.
 *
 * Each section is its own `PulseCard(DATA)` — this app's card system, matching the "one card per
 * list entry" shape `StockAnalysisGlossaryBottomSheet`/`MetricDetailScreen`'s `BandRow` already use,
 * rather than bare `Text` blocks directly on the screen background.
 *
 * No `Scaffold`/`TopAppBar`: that filled, elevated bar chrome is gone app-wide for Tutorials and
 * every other screen reached from Settings (see `MainActivity`'s `isPushedDestination` list: the
 * shared bottom nav is suppressed for all of these). [title] renders to the right of a bare
 * (unstyled, no background/elevation) back arrow via `PulseBackTitleRow` -- navigability is
 * preserved, just without the Material AppBar surface.
 */
@Composable
fun DocumentSectionsScreen(
    title: String,
    sections: List<Pair<String, String>>,
    onNavigateUp: () -> Unit,
    lastUpdatedLabel: String? = null,
    showFooter: Boolean = false,
    // Lets a caller drop one of the inline TutorialDiagrams composables right after a specific
    // section's card, keyed by that section's index in `sections` -- optional and empty by
    // default so every existing call site (Terms & Conditions, Privacy Policy, the other 4
    // Tutorials articles) keeps compiling unchanged.
    diagrams: Map<Int, @Composable () -> Unit> = emptyMap(),
    // Extra content rendered after the last section card (e.g. About's app-info block and its
    // links) -- optional so every existing call site keeps compiling unchanged.
    trailingContent: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        PulseBackTitleRow(title = title, onNavigateUp = onNavigateUp)
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            if (lastUpdatedLabel != null) {
                Text(
                    text = lastUpdatedLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalPulseColors.current.onSurfaceMuted
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))
            }

            sections.forEachIndexed { index, (heading, body) ->
                PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                        if (heading.isNotBlank()) {
                            Text(
                                text = heading,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                        }
                        FormattedBodyText(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                diagrams[index]?.let { diagram ->
                    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                            diagram()
                        }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                }
            }

            trailingContent?.invoke()

            if (showFooter) {
                DisclaimerFooter()
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))
        }
    }
}
