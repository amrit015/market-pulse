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

/** The sub-heading a [ConceptArticle] is listed under, within the Tutorials hub's "Market concepts" section. */
enum class ConceptSubGroup(@param:StringRes val titleRes: Int) {
    CYCLES_AND_STRUCTURE(R.string.tutorials_hub_subgroup_cycles_structure),
    SENTIMENT_AND_BEHAVIOUR(R.string.tutorials_hub_subgroup_sentiment_behaviour),
    VALUATION_AND_RATES(R.string.tutorials_hub_subgroup_valuation_rates),
    RISK_AND_PRICE_ACTION(R.string.tutorials_hub_subgroup_risk_price_action),
    MARKET_FUNDAMENTALS(R.string.tutorials_hub_subgroup_market_fundamentals)
}

/**
 * The concept articles shown in the Tutorials hub's "Market concepts" section, each as a card
 * carousel. [routeKey] is the stable navigation-argument value; [subGroup] picks which sub-heading
 * lists it. Every article shares one shape -- "What this is" / "Key ideas" / "In Market Pulse" /
 * "What it doesn't tell you".
 */
enum class ConceptArticle(
    val routeKey: String,
    @param:StringRes val titleRes: Int,
    val subGroup: ConceptSubGroup,
    val cards: List<ConceptCard>
) {
    BULL_BEAR_MARKETS(
        "bull_bear_markets", R.string.concept_bull_bear_markets_title, ConceptSubGroup.CYCLES_AND_STRUCTURE,
        full("bull_bear_markets")
    ),
    MARKET_TOPS(
        "market_tops", R.string.concept_market_tops_title, ConceptSubGroup.CYCLES_AND_STRUCTURE,
        full("market_tops")
    ),
    MARKET_BOTTOMS_CAPITULATION(
        "market_bottoms_capitulation", R.string.concept_market_bottoms_capitulation_title, ConceptSubGroup.CYCLES_AND_STRUCTURE,
        full("market_bottoms_capitulation")
    ),
    MARKET_PHASES(
        "market_phases", R.string.concept_market_phases_title, ConceptSubGroup.CYCLES_AND_STRUCTURE,
        full("market_phases")
    ),
    CONTRARIAN_INVESTING(
        "contrarian_investing", R.string.concept_contrarian_investing_title, ConceptSubGroup.SENTIMENT_AND_BEHAVIOUR,
        full("contrarian_investing")
    ),
    ROLE_OF_LIQUIDITY(
        "role_of_liquidity", R.string.concept_role_of_liquidity_title, ConceptSubGroup.SENTIMENT_AND_BEHAVIOUR,
        full("role_of_liquidity")
    ),
    SENTIMENT_POSITIONING(
        "sentiment_positioning", R.string.concept_sentiment_positioning_title, ConceptSubGroup.SENTIMENT_AND_BEHAVIOUR,
        full("sentiment_positioning")
    ),
    PE_EXPANSION_CONTRACTION(
        "pe_expansion_contraction", R.string.concept_pe_expansion_contraction_title, ConceptSubGroup.VALUATION_AND_RATES,
        full("pe_expansion_contraction")
    ),
    GROWTH_VS_VALUE(
        "growth_vs_value", R.string.concept_growth_vs_value_title, ConceptSubGroup.VALUATION_AND_RATES,
        full("growth_vs_value")
    ),
    FED_POLICY_CYCLES(
        "fed_policy_cycles", R.string.concept_fed_policy_cycles_title, ConceptSubGroup.VALUATION_AND_RATES,
        full("fed_policy_cycles")
    ),
    VALUATION_BASICS(
        "valuation_basics", R.string.concept_valuation_basics_title, ConceptSubGroup.VALUATION_AND_RATES,
        full("valuation_basics")
    ),
    VOLATILITY_REGIMES(
        "volatility_regimes", R.string.concept_volatility_regimes_title, ConceptSubGroup.RISK_AND_PRICE_ACTION,
        full("volatility_regimes")
    ),
    RELATIVE_STRENGTH(
        "relative_strength", R.string.concept_relative_strength_title, ConceptSubGroup.RISK_AND_PRICE_ACTION,
        full("relative_strength")
    ),
    CORRELATION_BREAKDOWN(
        "correlation_breakdown", R.string.concept_correlation_breakdown_title, ConceptSubGroup.RISK_AND_PRICE_ACTION,
        full("correlation_breakdown")
    ),
    MACRO_RATES(
        "macro_rates", R.string.concept_macro_rates_title, ConceptSubGroup.MARKET_FUNDAMENTALS,
        full("macro_rates", keyIdeasDiagram = ConceptDiagram.YIELD_CURVE)
    ),
    TECHNICAL_CONCEPTS(
        "technical_concepts", R.string.concept_technical_concepts_title, ConceptSubGroup.MARKET_FUNDAMENTALS,
        full("technical_concepts", keyIdeasDiagram = ConceptDiagram.SMA_EXTENSION)
    ),
    SUPPORT_RESISTANCE(
        "support_resistance", R.string.concept_support_resistance_title, ConceptSubGroup.MARKET_FUNDAMENTALS,
        full("support_resistance", inMarketPulseDiagram = ConceptDiagram.SMA_EXTENSION)
    ),
    MEAN_REVERSION_MOMENTUM(
        "mean_reversion_momentum", R.string.concept_mean_reversion_momentum_title, ConceptSubGroup.MARKET_FUNDAMENTALS,
        full("mean_reversion_momentum")
    ),
    EARNINGS_IMPACT(
        "earnings_impact", R.string.concept_earnings_impact_title, ConceptSubGroup.MARKET_FUNDAMENTALS,
        full("earnings_impact")
    ),
    SECTOR_ROTATION(
        "sector_rotation", R.string.concept_sector_rotation_title, ConceptSubGroup.MARKET_FUNDAMENTALS,
        full("sector_rotation")
    ),
    RATE_SENSITIVITY(
        "rate_sensitivity", R.string.concept_rate_sensitivity_title, ConceptSubGroup.MARKET_FUNDAMENTALS,
        full("rate_sensitivity")
    ),
    BREADTH_DIVERGENCE(
        "breadth_divergence", R.string.concept_breadth_divergence_title, ConceptSubGroup.MARKET_FUNDAMENTALS,
        full("breadth_divergence")
    );

    companion object {
        fun fromRouteKey(key: String?): ConceptArticle? = entries.firstOrNull { it.routeKey == key }
    }
}

/** The one shape every concept article shares; bodies are looked up by name because each topic's four strings share a prefix. */
private fun full(key: String, keyIdeasDiagram: ConceptDiagram? = null, inMarketPulseDiagram: ConceptDiagram? = null) = listOf(
    ConceptCard(R.string.deck_card_title_what_this_is, fullBody(key, 0)),
    ConceptCard(R.string.concept_card_title_key_ideas, fullBody(key, 1), keyIdeasDiagram),
    ConceptCard(R.string.concept_article_in_market_pulse, fullBody(key, 2), inMarketPulseDiagram),
    ConceptCard(R.string.concept_article_doesnt_tell_you, fullBody(key, 3))
)

@StringRes
private fun fullBody(key: String, index: Int): Int = when (key) {
    "bull_bear_markets" -> listOf(
        R.string.concept_bull_bear_markets_s0_body, R.string.concept_bull_bear_markets_s1_body,
        R.string.concept_bull_bear_markets_s2_body, R.string.concept_bull_bear_markets_s3_body
    )
    "market_tops" -> listOf(
        R.string.concept_market_tops_s0_body, R.string.concept_market_tops_s1_body,
        R.string.concept_market_tops_s2_body, R.string.concept_market_tops_s3_body
    )
    "market_bottoms_capitulation" -> listOf(
        R.string.concept_market_bottoms_capitulation_s0_body, R.string.concept_market_bottoms_capitulation_s1_body,
        R.string.concept_market_bottoms_capitulation_s2_body, R.string.concept_market_bottoms_capitulation_s3_body
    )
    "market_phases" -> listOf(
        R.string.concept_market_phases_s0_body, R.string.concept_market_phases_s1_body,
        R.string.concept_market_phases_s2_body, R.string.concept_market_phases_s3_body
    )
    "contrarian_investing" -> listOf(
        R.string.concept_contrarian_investing_s0_body, R.string.concept_contrarian_investing_s1_body,
        R.string.concept_contrarian_investing_s2_body, R.string.concept_contrarian_investing_s3_body
    )
    "role_of_liquidity" -> listOf(
        R.string.concept_role_of_liquidity_s0_body, R.string.concept_role_of_liquidity_s1_body,
        R.string.concept_role_of_liquidity_s2_body, R.string.concept_role_of_liquidity_s3_body
    )
    "sentiment_positioning" -> listOf(
        R.string.concept_sentiment_positioning_s0_body, R.string.concept_sentiment_positioning_s1_body,
        R.string.concept_sentiment_positioning_s2_body, R.string.concept_sentiment_positioning_s3_body
    )
    "pe_expansion_contraction" -> listOf(
        R.string.concept_pe_expansion_contraction_s0_body, R.string.concept_pe_expansion_contraction_s1_body,
        R.string.concept_pe_expansion_contraction_s2_body, R.string.concept_pe_expansion_contraction_s3_body
    )
    "growth_vs_value" -> listOf(
        R.string.concept_growth_vs_value_s0_body, R.string.concept_growth_vs_value_s1_body,
        R.string.concept_growth_vs_value_s2_body, R.string.concept_growth_vs_value_s3_body
    )
    "fed_policy_cycles" -> listOf(
        R.string.concept_fed_policy_cycles_s0_body, R.string.concept_fed_policy_cycles_s1_body,
        R.string.concept_fed_policy_cycles_s2_body, R.string.concept_fed_policy_cycles_s3_body
    )
    "valuation_basics" -> listOf(
        R.string.concept_valuation_basics_s0_body, R.string.concept_valuation_basics_s1_body,
        R.string.concept_valuation_basics_s2_body, R.string.concept_valuation_basics_s3_body
    )
    "volatility_regimes" -> listOf(
        R.string.concept_volatility_regimes_s0_body, R.string.concept_volatility_regimes_s1_body,
        R.string.concept_volatility_regimes_s2_body, R.string.concept_volatility_regimes_s3_body
    )
    "relative_strength" -> listOf(
        R.string.concept_relative_strength_s0_body, R.string.concept_relative_strength_s1_body,
        R.string.concept_relative_strength_s2_body, R.string.concept_relative_strength_s3_body
    )
    "correlation_breakdown" -> listOf(
        R.string.concept_correlation_breakdown_s0_body, R.string.concept_correlation_breakdown_s1_body,
        R.string.concept_correlation_breakdown_s2_body, R.string.concept_correlation_breakdown_s3_body
    )
    "macro_rates" -> listOf(
        R.string.concept_macro_rates_s0_body, R.string.concept_macro_rates_s1_body,
        R.string.concept_macro_rates_s2_body, R.string.concept_macro_rates_s3_body
    )
    "technical_concepts" -> listOf(
        R.string.concept_technical_concepts_s0_body, R.string.concept_technical_concepts_s1_body,
        R.string.concept_technical_concepts_s2_body, R.string.concept_technical_concepts_s3_body
    )
    "support_resistance" -> listOf(
        R.string.concept_support_resistance_s0_body, R.string.concept_support_resistance_key_ideas_body,
        R.string.concept_support_resistance_s1_body, R.string.concept_support_resistance_s2_body
    )
    "mean_reversion_momentum" -> listOf(
        R.string.concept_mean_reversion_momentum_s0_body, R.string.concept_mean_reversion_momentum_key_ideas_body,
        R.string.concept_mean_reversion_momentum_s1_body, R.string.concept_mean_reversion_momentum_s2_body
    )
    "earnings_impact" -> listOf(
        R.string.concept_earnings_impact_s0_body, R.string.concept_earnings_impact_key_ideas_body,
        R.string.concept_earnings_impact_s1_body, R.string.concept_earnings_impact_s2_body
    )
    "sector_rotation" -> listOf(
        R.string.concept_sector_rotation_s0_body, R.string.concept_sector_rotation_key_ideas_body,
        R.string.concept_sector_rotation_s1_body, R.string.concept_sector_rotation_s2_body
    )
    "rate_sensitivity" -> listOf(
        R.string.concept_rate_sensitivity_s0_body, R.string.concept_rate_sensitivity_key_ideas_body,
        R.string.concept_rate_sensitivity_s1_body, R.string.concept_rate_sensitivity_s2_body
    )
    else -> listOf(
        R.string.concept_breadth_divergence_s0_body, R.string.concept_breadth_divergence_key_ideas_body,
        R.string.concept_breadth_divergence_s1_body, R.string.concept_breadth_divergence_s2_body
    )
}[index]
