package com.marketlabs.pulse.core.learn

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * One Market Concepts article's four cards -- see `learn_content.json`'s own top-level comment for
 * the file's shape. [keyIdeasDiagram]/[inMarketPulseDiagram] name an inline diagram (resolved via
 * [learnDiagramFor]) for the one card each key names; at most one of the two is ever set today.
 */
@JsonClass(generateAdapter = true)
data class LearnConceptCardSet(
    @Json(name = "route_key") val routeKey: String,
    val title: String,
    @Json(name = "what_this_is") val whatThisIs: String,
    @Json(name = "key_ideas") val keyIdeas: String,
    @Json(name = "in_market_pulse") val inMarketPulse: String,
    @Json(name = "doesnt_tell_you") val doesntTellYou: String,
    @Json(name = "key_ideas_diagram") val keyIdeasDiagram: String? = null,
    @Json(name = "in_market_pulse_diagram") val inMarketPulseDiagram: String? = null
)

/** One Market Mechanisms deck's six cards. [theSignalsDiagram]/[whatToWatchDiagram] are the only two decks with an inline diagram today (Tactical Momentum, Systemic Risk respectively). */
@JsonClass(generateAdapter = true)
data class LearnMechanismDeck(
    @Json(name = "what_this_is") val whatThisIs: String,
    @Json(name = "the_signals") val theSignals: String,
    @Json(name = "how_to_read_together") val howToReadTogether: String,
    @Json(name = "what_to_watch") val whatToWatch: String,
    @Json(name = "common_misreads") val commonMisreads: String,
    @Json(name = "in_the_wild") val inTheWild: String,
    @Json(name = "the_signals_diagram") val theSignalsDiagram: String? = null,
    @Json(name = "what_to_watch_diagram") val whatToWatchDiagram: String? = null
)

/** One card of a standalone article (Gauge Anatomy, Data Limitations) -- unlike the two shapes
 * above, these don't share a fixed heading set, so each card carries its own [title]. */
@JsonClass(generateAdapter = true)
data class LearnCard(
    val title: String,
    val body: String,
    val diagram: String? = null
)

@JsonClass(generateAdapter = true)
data class LearnStandaloneArticle(
    val title: String,
    val cards: List<LearnCard>
)

/**
 * The raw shape of `learn_content.json`, deserialized as-is. [marketConcepts] is keyed by
 * [com.marketlabs.pulse.ui.screens.tutorials.ConceptSubGroup]'s JSON key (`cycles_and_structure`
 * etc.) -- this grouping is for the file's own readability; [ConceptArticle]'s `subGroup` field is
 * the one actually driving the hub's UI grouping; keep the two in sync by hand when adding an
 * article. [marketMechanisms] is keyed by [com.marketlabs.pulse.ui.components.tutorials.Mechanism]'s
 * `routeKey`.
 */
@JsonClass(generateAdapter = true)
data class LearnContentJson(
    @Json(name = "market_concepts") val marketConcepts: Map<String, List<LearnConceptCardSet>>,
    @Json(name = "market_mechanisms") val marketMechanisms: Map<String, LearnMechanismDeck>,
    @Json(name = "gauge_anatomy") val gaugeAnatomy: LearnStandaloneArticle,
    @Json(name = "data_limitations") val dataLimitations: LearnStandaloneArticle
)

/** Resolved, lookup-ready form of [LearnContentJson] -- [conceptArticles]/[mechanismDecks] flatten
 * the raw file's grouping into a direct `routeKey -> content` map, since every consumer looks up by
 * one specific article/deck's route key, never by group. */
data class LearnContent(
    val conceptArticles: Map<String, LearnConceptCardSet>,
    val mechanismDecks: Map<String, LearnMechanismDeck>,
    val gaugeAnatomy: LearnStandaloneArticle,
    val dataLimitations: LearnStandaloneArticle
)

internal fun LearnContentJson.resolve(): LearnContent = LearnContent(
    conceptArticles = marketConcepts.values.flatten().associateBy { it.routeKey },
    mechanismDecks = marketMechanisms,
    gaugeAnatomy = gaugeAnatomy,
    dataLimitations = dataLimitations
)
