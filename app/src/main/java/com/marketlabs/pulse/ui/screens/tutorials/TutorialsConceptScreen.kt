package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.learn.LearnContentProvider
import com.marketlabs.pulse.ui.components.tutorials.DeckPage
import com.marketlabs.pulse.ui.components.tutorials.learnDiagramFor
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * One concept article, from either hub section, as the same centered, equal-height card carousel
 * the mechanism decks use. Title and card bodies come from `learn_content.json` (via
 * [LearnContentProvider], keyed by [ConceptArticle.routeKey]); the four card headings are the
 * shared string resources every article reuses. Hand-authored static text, descriptive of what
 * market participants generally do or believe -- never a recommendation. Each names the app gauge
 * it's anchored to; if that gauge's methodology changes, the article needs the same edit.
 */
@Composable
fun TutorialsConceptScreen(article: ConceptArticle, onNavigateUp: () -> Unit, onNavigateToRoute: (String) -> Unit = {}) {
    val content = LearnContentProvider.get(LocalContext.current).conceptArticles[article.routeKey] ?: return

    val pages = listOf(
        DeckPage(title = stringResource(id = R.string.deck_card_title_what_this_is), body = content.whatThisIs),
        DeckPage(
            title = stringResource(id = R.string.concept_card_title_key_ideas),
            body = content.keyIdeas,
            diagram = learnDiagramFor(content.keyIdeasDiagram)
        ),
        DeckPage(
            title = stringResource(id = R.string.concept_article_in_market_pulse),
            body = content.inMarketPulse,
            diagram = learnDiagramFor(content.inMarketPulseDiagram),
            routes = content.inMarketPulseRoutes
        ),
        DeckPage(title = stringResource(id = R.string.concept_article_doesnt_tell_you), body = content.doesntTellYou)
    )

    CarouselArticleScreen(title = content.title, pages = pages, onNavigateUp = onNavigateUp, onNavigateToRoute = onNavigateToRoute)
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
