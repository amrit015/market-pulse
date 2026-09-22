package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.tutorials.DeckPage
import com.marketlabs.pulse.ui.components.tutorials.GaugeAnatomyDiagram
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Standalone Tutorials article on how to read a gauge: most gauges read their raw *level* against
 * fixed bands, but a few read direction-of-change instead, and two are polarity-inverted. This
 * teaches the one shared pattern (raw value -> band -> color), names the exceptions, and follows
 * the 3-step explanation with an inline diagram as its visual spine.
 *
 * Also carries two naming-collision glossary notes ("positioning," "market regime") as a
 * closing section -- this app has no live tap target that naturally reaches a definition-only entry
 * with no bands/no metric id, so rather than force new navigation plumbing for two low-priority
 * notes, they're placed here: a primer about how to read this app's own terminology is the most
 * natural home for "two words this app uses twice."
 */
@Composable
fun TutorialsGaugeAnatomyScreen(onNavigateUp: () -> Unit) {
    val pages = listOf(
        DeckPage(
            title = stringResource(id = R.string.tutorials_gauge_anatomy_steps_title),
            body = stringResource(id = R.string.tutorials_gauge_anatomy_steps_body),
            diagram = { GaugeAnatomyDiagram() }
        ),
        DeckPage(
            title = stringResource(id = R.string.tutorials_gauge_anatomy_exceptions_title),
            body = stringResource(id = R.string.tutorials_gauge_anatomy_exceptions_body)
        ),
        DeckPage(
            title = stringResource(id = R.string.tutorials_gauge_anatomy_naming_title),
            body = stringResource(id = R.string.tutorials_gauge_anatomy_naming_body)
        )
    )

    CarouselArticleScreen(
        title = stringResource(id = R.string.tutorials_item_gauge_anatomy),
        pages = pages,
        onNavigateUp = onNavigateUp
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewTutorialsGaugeAnatomyScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        TutorialsGaugeAnatomyScreen(onNavigateUp = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewTutorialsGaugeAnatomyScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        TutorialsGaugeAnatomyScreen(onNavigateUp = {})
    }
}
