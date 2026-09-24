package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.ui.components.tutorials.DeckPage
import com.marketlabs.pulse.ui.components.tutorials.SmaExtensionDiagram
import com.marketlabs.pulse.ui.components.tutorials.YieldCurveDiagram
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * One concept article, from either hub section, as the same centered, equal-height card carousel
 * the mechanism decks use (see [ConceptArticle] for each article's cards). Hand-authored
 * static text, descriptive of what market participants generally do or believe -- never a
 * recommendation. Each names the app gauge it's anchored to; if that gauge's methodology changes,
 * the article needs the same edit.
 */
@Composable
fun TutorialsConceptScreen(article: ConceptArticle, onNavigateUp: () -> Unit) {
    val pages = article.cards.map { card ->
        DeckPage(
            title = stringResource(id = card.titleRes),
            body = stringResource(id = card.bodyRes),
            diagram = when (card.diagram) {
                ConceptDiagram.SMA_EXTENSION -> ({ SmaExtensionDiagram() })
                ConceptDiagram.YIELD_CURVE -> ({ YieldCurveDiagram() })
                null -> null
            }
        )
    }

    CarouselArticleScreen(title = stringResource(id = article.titleRes), pages = pages, onNavigateUp = onNavigateUp)
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewTutorialsConceptScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        TutorialsConceptScreen(article = ConceptArticle.SUPPORT_RESISTANCE, onNavigateUp = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewTutorialsConceptScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        TutorialsConceptScreen(article = ConceptArticle.BREADTH_DIVERGENCE, onNavigateUp = {})
    }
}
