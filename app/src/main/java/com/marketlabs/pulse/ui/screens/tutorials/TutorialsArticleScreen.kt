package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.core.learn.IndicatorArticlesProvider
import com.marketlabs.pulse.ui.components.tutorials.DeckPage
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Generic pushed destination for any one of the 35 indicator + 5 event articles in
 * `indicator_articles.json`, looked up by [articleKey] (a `metric_id` or event id). Unlike Gauge
 * Anatomy/Data Limitations -- each its own hardcoded field + route + screen -- these 40 articles
 * share this one screen/route (`tutorials_article/{key}`), since hardcoding 40 routes/screens for
 * the same card-carousel shape would be pure repetition. [articleKey] not resolving to a real entry
 * (a typo, or a stale link) renders nothing rather than crashing -- same defensive shape
 * `TutorialsGaugesViewModel` and friends already use for an unresolved route argument.
 */
@Composable
fun TutorialsArticleScreen(articleKey: String, onNavigateUp: () -> Unit) {
    val article = IndicatorArticlesProvider.get(LocalContext.current)[articleKey] ?: return
    val pages = article.cards.map { card -> DeckPage(title = card.title, body = card.body) }

    CarouselArticleScreen(title = article.title, pages = pages, onNavigateUp = onNavigateUp)
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewTutorialsArticleScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        TutorialsArticleScreen(articleKey = "cpi_yoy", onNavigateUp = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewTutorialsArticleScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        TutorialsArticleScreen(articleKey = "cpi_yoy", onNavigateUp = {})
    }
}
