package com.marketlabs.pulse.ui.components.tutorials

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.tutorials.SentimentGaugesDiagram
import com.marketlabs.pulse.ui.components.tutorials.YieldCurveDiagram

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
 * Card copy is hand-authored static text, kept to descriptions of conditions -- never
 * recommendations. Every threshold quoted in it must match the backend's own constant for that
 * gauge; when a gauge's bands change, the matching card here needs the same edit.
 */
@Composable
fun mechanismContentPages(mechanism: Mechanism): List<DeckPage> {
    val titles = listOf(
        R.string.deck_card_title_what_this_is,
        R.string.deck_card_title_signals,
        R.string.deck_card_title_together,
        R.string.deck_card_title_watch,
        R.string.deck_card_title_misreads,
        R.string.deck_card_title_wild
    )
    val bodies = bodyResFor(mechanism)
    return titles.indices.map { index ->
        DeckPage(
            title = stringResource(id = titles[index]),
            body = stringResource(id = bodies[index]),
            diagram = diagramFor(mechanism, cardIndex = index)
        )
    }
}

/** Card 2 of Tactical Momentum carries the sentiment bars (Fear & Greed, Put/Call, VIX); card 4 of
 * Systemic Risk carries the yield-curve inversion sketch next to its "yield curve crossing zero" bullet. */
private fun diagramFor(mechanism: Mechanism, cardIndex: Int): (@Composable () -> Unit)? = when {
    mechanism == Mechanism.TACTICAL_MOMENTUM && cardIndex == 1 -> ({ SentimentGaugesDiagram() })
    mechanism == Mechanism.SYSTEMIC_RISK && cardIndex == 3 -> ({ YieldCurveDiagram() })
    else -> null
}

private fun bodyResFor(mechanism: Mechanism): List<Int> = when (mechanism) {
    Mechanism.TACTICAL_MOMENTUM -> listOf(
        R.string.deck_tactical_momentum_card1_body, R.string.deck_tactical_momentum_card2_body,
        R.string.deck_tactical_momentum_card3_body, R.string.deck_tactical_momentum_card4_body,
        R.string.deck_tactical_momentum_card5_body, R.string.deck_tactical_momentum_card6_body
    )
    Mechanism.SYSTEMIC_RISK -> listOf(
        R.string.deck_systemic_risk_card1_body, R.string.deck_systemic_risk_card2_body,
        R.string.deck_systemic_risk_card3_body, R.string.deck_systemic_risk_card4_body,
        R.string.deck_systemic_risk_card5_body, R.string.deck_systemic_risk_card6_body
    )
    Mechanism.VALUATION -> listOf(
        R.string.deck_valuation_card1_body, R.string.deck_valuation_card2_body,
        R.string.deck_valuation_card3_body, R.string.deck_valuation_card4_body,
        R.string.deck_valuation_card5_body, R.string.deck_valuation_card6_body
    )
    Mechanism.MACRO_VITALS -> listOf(
        R.string.deck_macro_vitals_card1_body, R.string.deck_macro_vitals_card2_body,
        R.string.deck_macro_vitals_card3_body, R.string.deck_macro_vitals_card4_body,
        R.string.deck_macro_vitals_card5_body, R.string.deck_macro_vitals_card6_body
    )
    Mechanism.POSTURE -> listOf(
        R.string.deck_posture_card1_body, R.string.deck_posture_card2_body,
        R.string.deck_posture_card3_body, R.string.deck_posture_card4_body,
        R.string.deck_posture_card5_body, R.string.deck_posture_card6_body
    )
    Mechanism.POSITIONING -> listOf(
        R.string.deck_positioning_card1_body, R.string.deck_positioning_card2_body,
        R.string.deck_positioning_card3_body, R.string.deck_positioning_card4_body,
        R.string.deck_positioning_card5_body, R.string.deck_positioning_card6_body
    )
    Mechanism.STOCK_ANALYSIS -> listOf(
        R.string.deck_stock_analysis_card1_body, R.string.deck_stock_analysis_card2_body,
        R.string.deck_stock_analysis_card3_body, R.string.deck_stock_analysis_card4_body,
        R.string.deck_stock_analysis_card5_body, R.string.deck_stock_analysis_card6_body
    )
}
