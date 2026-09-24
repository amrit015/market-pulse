package com.marketlabs.pulse.ui.screens.tutorials

import androidx.annotation.StringRes
import com.marketlabs.pulse.R

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
 * carousel. [routeKey] is the stable navigation-argument value, and the key into
 * `learn_content.json`'s `market_concepts` entries (via `LearnContentProvider` -- see
 * `TutorialsConceptScreen` for where the title/cards actually get resolved). [subGroup] picks which
 * sub-heading lists it in the hub -- keep this in sync by hand with whichever group the JSON file
 * nests the same [routeKey] under; see `LearnContentJson`'s own doc comment for why the file
 * duplicates this grouping instead of deriving it from here.
 *
 * Every article shares one card shape -- "What this is" / "Key ideas" / "In Market Pulse" /
 * "What it doesn't tell you" -- so there's nothing else to carry per entry.
 */
enum class ConceptArticle(val routeKey: String, val subGroup: ConceptSubGroup) {
    BULL_BEAR_MARKETS("bull_bear_markets", ConceptSubGroup.CYCLES_AND_STRUCTURE),
    MARKET_TOPS("market_tops", ConceptSubGroup.CYCLES_AND_STRUCTURE),
    MARKET_BOTTOMS_CAPITULATION("market_bottoms_capitulation", ConceptSubGroup.CYCLES_AND_STRUCTURE),
    MARKET_PHASES("market_phases", ConceptSubGroup.CYCLES_AND_STRUCTURE),
    CONTRARIAN_INVESTING("contrarian_investing", ConceptSubGroup.SENTIMENT_AND_BEHAVIOUR),
    ROLE_OF_LIQUIDITY("role_of_liquidity", ConceptSubGroup.SENTIMENT_AND_BEHAVIOUR),
    SENTIMENT_POSITIONING("sentiment_positioning", ConceptSubGroup.SENTIMENT_AND_BEHAVIOUR),
    PE_EXPANSION_CONTRACTION("pe_expansion_contraction", ConceptSubGroup.VALUATION_AND_RATES),
    GROWTH_VS_VALUE("growth_vs_value", ConceptSubGroup.VALUATION_AND_RATES),
    FED_POLICY_CYCLES("fed_policy_cycles", ConceptSubGroup.VALUATION_AND_RATES),
    VALUATION_BASICS("valuation_basics", ConceptSubGroup.VALUATION_AND_RATES),
    VOLATILITY_REGIMES("volatility_regimes", ConceptSubGroup.RISK_AND_PRICE_ACTION),
    RELATIVE_STRENGTH("relative_strength", ConceptSubGroup.RISK_AND_PRICE_ACTION),
    CORRELATION_BREAKDOWN("correlation_breakdown", ConceptSubGroup.RISK_AND_PRICE_ACTION),
    MACRO_RATES("macro_rates", ConceptSubGroup.MARKET_FUNDAMENTALS),
    TECHNICAL_CONCEPTS("technical_concepts", ConceptSubGroup.MARKET_FUNDAMENTALS),
    SUPPORT_RESISTANCE("support_resistance", ConceptSubGroup.MARKET_FUNDAMENTALS),
    MEAN_REVERSION_MOMENTUM("mean_reversion_momentum", ConceptSubGroup.MARKET_FUNDAMENTALS),
    EARNINGS_IMPACT("earnings_impact", ConceptSubGroup.MARKET_FUNDAMENTALS),
    SECTOR_ROTATION("sector_rotation", ConceptSubGroup.MARKET_FUNDAMENTALS),
    RATE_SENSITIVITY("rate_sensitivity", ConceptSubGroup.MARKET_FUNDAMENTALS),
    BREADTH_DIVERGENCE("breadth_divergence", ConceptSubGroup.MARKET_FUNDAMENTALS);

    companion object {
        fun fromRouteKey(key: String?): ConceptArticle? = entries.firstOrNull { it.routeKey == key }
    }
}
