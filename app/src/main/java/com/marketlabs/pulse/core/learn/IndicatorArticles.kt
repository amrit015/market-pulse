package com.marketlabs.pulse.core.learn

import com.squareup.moshi.JsonClass

/** One card of an indicator/event article -- same shape as [LearnCard] minus [LearnCard.diagram]
 * (none of these 40 articles carry an inline diagram). */
@JsonClass(generateAdapter = true)
data class IndicatorArticleCard(val title: String, val body: String)

/**
 * One article in `assets/indicator_articles.json` -- fixed 5-card structure (What it measures /
 * How to read it / What to watch / Scenarios / Gotchas) for every entry, though nothing in this
 * shape enforces that fixed set the way [LearnConceptCardSet]/[LearnMechanismDeck] enforce theirs;
 * [IndicatorArticleCard.title] just carries whatever heading each card actually has, same as
 * [LearnCard]. [subtitle] is the grid-tile description used both by a mechanism deck's own "See
 * all indicators" ([com.marketlabs.pulse.ui.screens.tutorials.TutorialsGaugesScreen]) and by the
 * Learn hub's "Economic Events" section.
 *
 * The file is keyed flatly by id -- 35 entries by `metric_id` (matching
 * [com.marketlabs.pulse.ui.screens.tutorials.TutorialsGaugesCatalog]'s own inventory exactly) plus
 * 5 event-only ids (`ism_manufacturing_pmi`, `ism_services_pmi`, `ppi`, `initial_jobless_claims`,
 * `fomc_rate_decision`) that have no corresponding gauge. This mirrors `metric_glossary.json`'s own
 * flat-map-by-id shape rather than [LearnContentJson]'s grouped one, since deck-family grouping is
 * already owned by `TutorialsGaugesCatalog` -- duplicating it here would be two sources of truth
 * for the same grouping.
 */
@JsonClass(generateAdapter = true)
data class IndicatorArticle(
    val title: String,
    val subtitle: String,
    val cards: List<IndicatorArticleCard>
)
