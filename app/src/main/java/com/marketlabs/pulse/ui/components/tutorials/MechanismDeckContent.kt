package com.marketlabs.pulse.ui.components.tutorials

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.learn.LearnContentProvider
import com.marketlabs.pulse.core.learn.LearnMechanismDeck

/** One card of a [CardCarousel]: a heading, body text, and an optional inline diagram. */
data class DeckPage(
    val title: String,
    val body: String,
    val diagram: (@Composable () -> Unit)? = null
)

/**
 * The six fixed content cards of a mechanism deck, in order: What this is, The signals, How to read
 * them together, What to watch, Common misreads, In the wild. The "See all indicators" link is not
 * a card -- the deck screen shows it standalone below the carousel.
 *
 * Card copy comes from `learn_content.json` (via [LearnContentProvider], keyed by
 * [Mechanism.routeKey]) -- hand-authored static text, kept to descriptions of conditions -- never
 * recommendations. Every threshold quoted in it must match the backend's own constant for that
 * gauge; when a gauge's bands change, the matching card in the JSON needs the same edit.
 */
@Composable
fun mechanismContentPages(mechanism: Mechanism): List<DeckPage> {
    val deck = LearnContentProvider.get(LocalContext.current).mechanismDecks[mechanism.routeKey] ?: return emptyList()

    val titles = listOf(
        R.string.deck_card_title_what_this_is,
        R.string.deck_card_title_signals,
        R.string.deck_card_title_together,
        R.string.deck_card_title_watch,
        R.string.deck_card_title_misreads,
        R.string.deck_card_title_wild
    )
    val bodies = listOf(
        deck.whatThisIs, deck.theSignals, deck.howToReadTogether,
        deck.whatToWatch, deck.commonMisreads, deck.inTheWild
    )
    val diagramKeys = listOf(null, deck.theSignalsDiagram, null, deck.whatToWatchDiagram, null, null)

    return titles.indices.map { index ->
        DeckPage(
            title = stringResource(id = titles[index]),
            body = bodies[index],
            diagram = learnDiagramFor(diagramKeys[index])
        )
    }
}
