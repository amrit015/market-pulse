package com.marketlabs.pulse.core.learn

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** One "open in app" chip -- [route] is a [com.marketlabs.pulse.ui.navigation.PulseRoutes] constant
 * NAME as authored in `learn_content.json` (e.g. "MARKET_INDICATORS"), not its route-string value;
 * resolving the name to a real route is `PulseNavGraph`'s job, checked by
 * `LearnContentJsonTest.everyRouteChipTargetIsARealPulseRoutesConstant`. [label] is the chip's own
 * display text, which can name something more specific than the route it lands on (e.g. "Tactical
 * Momentum" landing on the whole Indicators screen, since `PulseRoutes` has no sub-tab routes). */
@JsonClass(generateAdapter = true)
data class LearnRouteChip(val route: String, val label: String)

/**
 * One Market Concepts article's four cards -- see `learn_content.json`'s own top-level comment for
 * the file's shape. [subtitle] is a one-line hub-grid-tile description, distinct from any card body.
 * [keyIdeasDiagram]/[inMarketPulseDiagram] name an inline diagram (resolved via [learnDiagramFor])
 * for the one card each key names; at most one of the two is ever set today. [inMarketPulseRoutes]
 * is the one card whose prose currently names real screens by name -- mirrors the `<slot>_diagram`
 * pattern rather than a new shape.
 */
@JsonClass(generateAdapter = true)
data class LearnConceptCardSet(
    @Json(name = "route_key") val routeKey: String,
    val title: String,
    val subtitle: String,
    @Json(name = "what_this_is") val whatThisIs: String,
    @Json(name = "key_ideas") val keyIdeas: String,
    @Json(name = "in_market_pulse") val inMarketPulse: String,
    @Json(name = "doesnt_tell_you") val doesntTellYou: String,
    @Json(name = "key_ideas_diagram") val keyIdeasDiagram: String? = null,
    @Json(name = "in_market_pulse_diagram") val inMarketPulseDiagram: String? = null,
    @Json(name = "in_market_pulse_routes") val inMarketPulseRoutes: List<LearnRouteChip>? = null
)

/** One Market Mechanisms deck's six cards. [subtitle] is a one-line hub-grid-tile description --
 * mechanism decks have no `title` of their own (that comes from the [com.marketlabs.pulse.ui.
 * components.tutorials.Mechanism] enum's `titleRes`), so this is the one piece of deck-level copy
 * that does live in JSON. [theSignalsDiagram]/[whatToWatchDiagram]/[howToReadTogetherDiagram] are
 * the only decks with an inline diagram today; [theSignalsRoutes]/[whatToWatchRoutes]/
 * [howToReadTogetherRoutes] are the matching "open in app" chip slots, populated only where a
 * card's own prose names real screens. */
@JsonClass(generateAdapter = true)
data class LearnMechanismDeck(
    val subtitle: String,
    @Json(name = "what_this_is") val whatThisIs: String,
    @Json(name = "the_signals") val theSignals: String,
    @Json(name = "how_to_read_together") val howToReadTogether: String,
    @Json(name = "what_to_watch") val whatToWatch: String,
    @Json(name = "common_misreads") val commonMisreads: String,
    @Json(name = "in_the_wild") val inTheWild: String,
    @Json(name = "the_signals_diagram") val theSignalsDiagram: String? = null,
    @Json(name = "what_to_watch_diagram") val whatToWatchDiagram: String? = null,
    @Json(name = "how_to_read_together_diagram") val howToReadTogetherDiagram: String? = null,
    @Json(name = "the_signals_routes") val theSignalsRoutes: List<LearnRouteChip>? = null,
    @Json(name = "what_to_watch_routes") val whatToWatchRoutes: List<LearnRouteChip>? = null,
    @Json(name = "how_to_read_together_routes") val howToReadTogetherRoutes: List<LearnRouteChip>? = null
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

/** A title + one-line subtitle for a piece of hub structure that has no other content of its own --
 * a top-level hub section ("Market concepts", "Economic events", ...) or a
 * [com.marketlabs.pulse.ui.screens.tutorials.ConceptSubGroup] category heading. Both used to be
 * string-resource pairs (title/subtitle alongside each other) purely because their Kotlin-side
 * grouping had no JSON node to attach a subtitle to; now that both live here, they're content like
 * every other title/subtitle in this file, not special-cased. */
@JsonClass(generateAdapter = true)
data class LearnSectionHeading(val title: String, val subtitle: String)

/**
 * The raw shape of `learn_content.json`, deserialized as-is. [marketConcepts] is keyed by
 * [com.marketlabs.pulse.ui.screens.tutorials.ConceptSubGroup]'s JSON key (`cycles_and_structure`
 * etc.) -- this grouping is for the file's own readability; [ConceptArticle]'s `subGroup` field is
 * the one actually driving the hub's UI grouping; keep the two in sync by hand when adding an
 * article. [marketConceptsSubgroups] is keyed by that same JSON key, one [LearnSectionHeading] per
 * [com.marketlabs.pulse.ui.screens.tutorials.ConceptSubGroup] entry. [marketMechanisms] is keyed by
 * [com.marketlabs.pulse.ui.components.tutorials.Mechanism]'s `routeKey`. [hubSections] is keyed by a
 * fixed, hand-picked set of hub section ids (`start`, `market_concepts`, `economic_events`,
 * `mechanisms`, `data`) that only [com.marketlabs.pulse.ui.screens.tutorials.TutorialsHubScreen]
 * itself knows about -- there's no enum backing the hub's own top-level section order.
 */
@JsonClass(generateAdapter = true)
data class LearnContentJson(
    @Json(name = "market_concepts") val marketConcepts: Map<String, List<LearnConceptCardSet>>,
    @Json(name = "market_concepts_subgroups") val marketConceptsSubgroups: Map<String, LearnSectionHeading>,
    @Json(name = "market_mechanisms") val marketMechanisms: Map<String, LearnMechanismDeck>,
    @Json(name = "hub_sections") val hubSections: Map<String, LearnSectionHeading>,
    @Json(name = "gauge_anatomy") val gaugeAnatomy: LearnStandaloneArticle,
    @Json(name = "data_limitations") val dataLimitations: LearnStandaloneArticle
)

/** Resolved, lookup-ready form of [LearnContentJson] -- [conceptArticles]/[mechanismDecks] flatten
 * the raw file's grouping into a direct `routeKey -> content` map, since every consumer looks up by
 * one specific article/deck's route key, never by group. */
data class LearnContent(
    val conceptArticles: Map<String, LearnConceptCardSet>,
    val conceptSubgroupHeadings: Map<String, LearnSectionHeading>,
    val mechanismDecks: Map<String, LearnMechanismDeck>,
    val hubSections: Map<String, LearnSectionHeading>,
    val gaugeAnatomy: LearnStandaloneArticle,
    val dataLimitations: LearnStandaloneArticle
)

internal fun LearnContentJson.resolve(): LearnContent = LearnContent(
    conceptArticles = marketConcepts.values.flatten().associateBy { it.routeKey },
    conceptSubgroupHeadings = marketConceptsSubgroups,
    mechanismDecks = marketMechanisms,
    hubSections = hubSections,
    gaugeAnatomy = gaugeAnatomy,
    dataLimitations = dataLimitations
)
