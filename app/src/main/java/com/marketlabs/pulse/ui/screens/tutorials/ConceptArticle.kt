package com.marketlabs.pulse.ui.screens.tutorials

import androidx.annotation.StringRes
import com.marketlabs.pulse.R

/** An inline diagram a concept card can carry. */
enum class ConceptDiagram { SMA_EXTENSION, YIELD_CURVE }

/** One card of a concept article: a heading, its body text, and an optional diagram. */
data class ConceptCard(
    @param:StringRes val titleRes: Int,
    @param:StringRes val bodyRes: Int,
    val diagram: ConceptDiagram? = null
)

/**
 * The standard market concept articles (Tutorials -> Standard market concepts), each shown as a
 * card carousel. [routeKey] is the stable navigation-argument value.
 *
 * Two shapes: the broad topic overviews (sentiment, macro, technicals, valuation) are "What this
 * is" / "Key ideas" / "In Market Pulse"; the focused concept articles are "What this is" /
 * "In Market Pulse" / "What it doesn't tell you".
 */
enum class ConceptArticle(
    val routeKey: String,
    @param:StringRes val titleRes: Int,
    val cards: List<ConceptCard>
) {
    SENTIMENT_POSITIONING("sentiment_positioning", R.string.concept_sentiment_positioning_title, overview("sentiment_positioning")),
    MACRO_RATES("macro_rates", R.string.concept_macro_rates_title, overview("macro_rates", ConceptDiagram.YIELD_CURVE)),
    TECHNICAL_CONCEPTS("technical_concepts", R.string.concept_technical_concepts_title, overview("technical_concepts", ConceptDiagram.SMA_EXTENSION)),
    VALUATION_BASICS("valuation_basics", R.string.concept_valuation_basics_title, overview("valuation_basics")),
    SUPPORT_RESISTANCE(
        "support_resistance", R.string.concept_support_resistance_title,
        focused(
            R.string.concept_support_resistance_s0_body, R.string.concept_support_resistance_s1_body,
            R.string.concept_support_resistance_s2_body, ConceptDiagram.SMA_EXTENSION
        )
    ),
    MEAN_REVERSION_MOMENTUM(
        "mean_reversion_momentum", R.string.concept_mean_reversion_momentum_title,
        focused(
            R.string.concept_mean_reversion_momentum_s0_body, R.string.concept_mean_reversion_momentum_s1_body,
            R.string.concept_mean_reversion_momentum_s2_body
        )
    ),
    EARNINGS_IMPACT(
        "earnings_impact", R.string.concept_earnings_impact_title,
        focused(
            R.string.concept_earnings_impact_s0_body, R.string.concept_earnings_impact_s1_body,
            R.string.concept_earnings_impact_s2_body
        )
    ),
    SECTOR_ROTATION(
        "sector_rotation", R.string.concept_sector_rotation_title,
        focused(
            R.string.concept_sector_rotation_s0_body, R.string.concept_sector_rotation_s1_body,
            R.string.concept_sector_rotation_s2_body
        )
    ),
    RATE_SENSITIVITY(
        "rate_sensitivity", R.string.concept_rate_sensitivity_title,
        focused(
            R.string.concept_rate_sensitivity_s0_body, R.string.concept_rate_sensitivity_s1_body,
            R.string.concept_rate_sensitivity_s2_body
        )
    ),
    BREADTH_DIVERGENCE(
        "breadth_divergence", R.string.concept_breadth_divergence_title,
        focused(
            R.string.concept_breadth_divergence_s0_body, R.string.concept_breadth_divergence_s1_body,
            R.string.concept_breadth_divergence_s2_body
        )
    );

    companion object {
        fun fromRouteKey(key: String?): ConceptArticle? = entries.firstOrNull { it.routeKey == key }
    }
}

private fun focused(
    @StringRes intro: Int,
    @StringRes inMarketPulse: Int,
    @StringRes doesntTell: Int,
    inMarketPulseDiagram: ConceptDiagram? = null
) = listOf(
    ConceptCard(R.string.deck_card_title_what_this_is, intro),
    ConceptCard(R.string.concept_article_in_market_pulse, inMarketPulse, inMarketPulseDiagram),
    ConceptCard(R.string.concept_article_doesnt_tell_you, doesntTell)
)

/** The overview shape; bodies are looked up by name because each topic's three strings share a prefix. */
private fun overview(key: String, keyIdeasDiagram: ConceptDiagram? = null) = listOf(
    ConceptCard(R.string.deck_card_title_what_this_is, overviewBody(key, 0)),
    ConceptCard(R.string.concept_card_title_key_ideas, overviewBody(key, 1), keyIdeasDiagram),
    ConceptCard(R.string.concept_article_in_market_pulse, overviewBody(key, 2))
)

@StringRes
private fun overviewBody(key: String, index: Int): Int = when (key) {
    "sentiment_positioning" -> listOf(
        R.string.concept_sentiment_positioning_s0_body, R.string.concept_sentiment_positioning_s1_body,
        R.string.concept_sentiment_positioning_s2_body
    )
    "macro_rates" -> listOf(
        R.string.concept_macro_rates_s0_body, R.string.concept_macro_rates_s1_body, R.string.concept_macro_rates_s2_body
    )
    "technical_concepts" -> listOf(
        R.string.concept_technical_concepts_s0_body, R.string.concept_technical_concepts_s1_body,
        R.string.concept_technical_concepts_s2_body
    )
    else -> listOf(
        R.string.concept_valuation_basics_s0_body, R.string.concept_valuation_basics_s1_body,
        R.string.concept_valuation_basics_s2_body
    )
}[index]
