package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.core.learn.LearnContentProvider
import com.marketlabs.pulse.ui.components.tutorials.DeckPage
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/** Title and card come from `learn_content.json`'s `data_limitations` entry (via [LearnContentProvider]). */
@Composable
fun TutorialsDataLimitationsScreen(onNavigateUp: () -> Unit) {
    val article = LearnContentProvider.get(LocalContext.current).dataLimitations
    val pages = article.cards.map { card -> DeckPage(title = card.title, body = card.body) }

    CarouselArticleScreen(title = article.title, pages = pages, onNavigateUp = onNavigateUp)
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewTutorialsDataLimitationsScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        TutorialsDataLimitationsScreen(onNavigateUp = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewTutorialsDataLimitationsScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        TutorialsDataLimitationsScreen(onNavigateUp = {})
    }
}
