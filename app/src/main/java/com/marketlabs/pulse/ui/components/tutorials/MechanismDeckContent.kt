package com.marketlabs.pulse.ui.components.tutorials

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.learn.LearnContentProvider
import com.marketlabs.pulse.core.learn.LearnMechanismDeck
import com.marketlabs.pulse.core.learn.LearnRouteChip
import com.marketlabs.pulse.ui.screens.stocks.detail.ViewMoreRow

/** One card of a [CardCarousel]: a heading, body text, and an optional inline diagram. [tabLabel]
 * is what the top [com.marketlabs.pulse.ui.components.PulseTabRow] shows for this card -- defaults
 * to [title] (concept articles use their card names as-is), overridden only where a card's full
 * heading is too long for a tab (e.g. a mechanism deck's "How to read them together" -> "Read together").
 * [routes], when non-null/non-empty, renders "open in app" chips below the card's diagram (or body,
 * if it has none) for the handful of cards whose prose names real screens by name. [trailingContent]
 * is an arbitrary extra composable at the very bottom of the card, past routes/diagram -- currently
 * only "What this is"'s "See all indicators" link uses it. */
data class DeckPage(
    val title: String,
    val body: String,
    val diagram: (@Composable () -> Unit)? = null,
    val tabLabel: String = title,
    val routes: List<LearnRouteChip>? = null,
    val trailingContent: (@Composable () -> Unit)? = null
)

/**
 * The six fixed content cards of a mechanism deck, in order: What this is, The signals, How to read
 * them together, What to watch, Common misreads, In the wild. "See all indicators" ([onSeeIndicators])
 * lives inside the "What this is" card itself (its [DeckPage.trailingContent]), not as a separate
 * element below the whole carousel -- always this one mechanism's own indicators, never a combined
 * multi-mechanism list.
 *
 * Card copy comes from `learn_content.json` (via [LearnContentProvider], keyed by
 * [Mechanism.routeKey]) -- hand-authored static text, kept to descriptions of conditions -- never
 * recommendations. Every threshold quoted in it must match the backend's own constant for that
 * gauge; when a gauge's bands change, the matching card in the JSON needs the same edit.
 */
@Composable
fun mechanismContentPages(mechanism: Mechanism, onSeeIndicators: () -> Unit): List<DeckPage> {
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
    val diagramKeys = listOf(
        null, deck.theSignalsDiagram, deck.howToReadTogetherDiagram, deck.whatToWatchDiagram, null, null
    )
    val tabLabels = listOf(
        null, null, R.string.deck_card_tab_together, null, R.string.deck_card_tab_misreads, null
    )
    val routes = listOf(
        null, deck.theSignalsRoutes, deck.howToReadTogetherRoutes, deck.whatToWatchRoutes, null, null
    )
    val seeAllIndicatorsLabel = stringResource(id = R.string.deck_link_see_all_indicators)

    return titles.indices.map { index ->
        val title = stringResource(id = titles[index])
        DeckPage(
            title = title,
            body = bodies[index],
            diagram = learnDiagramFor(diagramKeys[index]),
            tabLabel = tabLabels[index]?.let { stringResource(id = it) } ?: title,
            routes = routes[index],
            trailingContent = if (index == 0) {
                { ViewMoreRow(text = seeAllIndicatorsLabel, onClick = onSeeIndicators) }
            } else {
                null
            }
        )
    }
}
