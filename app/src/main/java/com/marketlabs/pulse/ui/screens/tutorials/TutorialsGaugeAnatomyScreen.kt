package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.core.learn.LearnContentProvider
import com.marketlabs.pulse.ui.components.tutorials.DeckPage
import com.marketlabs.pulse.ui.components.tutorials.learnDiagramFor
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Standalone Tutorials article on how to read a gauge: most gauges read their raw *level* against
 * fixed bands, but a few read direction-of-change instead, and two are polarity-inverted. This
 * teaches the one shared pattern (raw value -> band -> color), names the exceptions, and follows
 * the 3-step explanation with an inline diagram as its visual spine. Title and cards come from
 * `learn_content.json`'s `gauge_anatomy` entry (via [LearnContentProvider]).
 *
 * Also carries two naming-collision glossary notes ("positioning," "market regime") as a
 * closing section -- this app has no live tap target that naturally reaches a definition-only entry
 * with no bands/no metric id, so rather than force new navigation plumbing for two low-priority
 * notes, they're placed here: a primer about how to read this app's own terminology is the most
 * natural home for "two words this app uses twice."
 */
@Composable
fun TutorialsGaugeAnatomyScreen(onNavigateUp: () -> Unit) {
    val article = LearnContentProvider.get(LocalContext.current).gaugeAnatomy
    val pages = article.cards.map { card ->
        DeckPage(title = card.title, body = card.body, diagram = learnDiagramFor(card.diagram))
    }

    CarouselArticleScreen(title = article.title, pages = pages, onNavigateUp = onNavigateUp)
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
