package com.marketlabs.pulse.core.learn

import com.marketlabs.pulse.ui.components.tutorials.KNOWN_DIAGRAM_KEYS
import com.marketlabs.pulse.ui.components.tutorials.learnDiagramFor
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Parses the real `assets/learn_content.json` (read straight off disk, not via Android's
 * AssetManager -- this is a plain JVM test, no Robolectric/instrumentation in this project) with
 * the same Moshi adapters `LearnContentProvider` uses at runtime, so a field-name mismatch between
 * this file and [LearnContentJson]'s `@Json(name = ...)` annotations fails a build instead of only
 * showing up as silently-empty content on a device.
 */
class LearnContentJsonTest {

    private fun loadContent(): LearnContent {
        val file = File("src/main/assets/learn_content.json")
        assertTrue("learn_content.json not found at ${file.absolutePath}", file.exists())
        val json = file.readText()
        val adapter = Moshi.Builder().build().adapter(LearnContentJson::class.java)
        val parsed = adapter.fromJson(json)
        assertNotNull("Moshi failed to parse learn_content.json", parsed)
        return parsed!!.resolve()
    }

    @Test
    fun allConceptArticlesParseWithNonBlankContent() {
        val content = loadContent()
        assertEquals(22, content.conceptArticles.size)
        content.conceptArticles.forEach { (routeKey, article) ->
            assertTrue("$routeKey title blank", article.title.isNotBlank())
            assertTrue("$routeKey subtitle blank", article.subtitle.isNotBlank())
            assertTrue("$routeKey whatThisIs blank", article.whatThisIs.isNotBlank())
            assertTrue("$routeKey keyIdeas blank", article.keyIdeas.isNotBlank())
            assertTrue("$routeKey inMarketPulse blank", article.inMarketPulse.isNotBlank())
            assertTrue("$routeKey doesntTellYou blank", article.doesntTellYou.isNotBlank())
        }
    }

    @Test
    fun allMechanismDecksParseWithNonBlankContent() {
        val content = loadContent()
        assertEquals(7, content.mechanismDecks.size)
        content.mechanismDecks.forEach { (routeKey, deck) ->
            assertTrue("$routeKey subtitle blank", deck.subtitle.isNotBlank())
            assertTrue("$routeKey whatThisIs blank", deck.whatThisIs.isNotBlank())
            assertTrue("$routeKey theSignals blank", deck.theSignals.isNotBlank())
            assertTrue("$routeKey howToReadTogether blank", deck.howToReadTogether.isNotBlank())
            assertTrue("$routeKey whatToWatch blank", deck.whatToWatch.isNotBlank())
            assertTrue("$routeKey commonMisreads blank", deck.commonMisreads.isNotBlank())
            assertTrue("$routeKey inTheWild blank", deck.inTheWild.isNotBlank())
        }
    }

    @Test
    fun hubSectionsHasAllFiveIdsWithNonBlankContent() {
        val content = loadContent()
        val expectedIds = setOf("start", "market_concepts", "economic_events", "mechanisms", "data")
        assertEquals(expectedIds, content.hubSections.keys)
        content.hubSections.forEach { (id, heading) ->
            assertTrue("$id title blank", heading.title.isNotBlank())
            assertTrue("$id subtitle blank", heading.subtitle.isNotBlank())
        }
    }

    @Test
    fun conceptSubgroupHeadingsMatchTheConceptSubGroupEnumExactly() {
        val content = loadContent()
        val enumJsonKeys = com.marketlabs.pulse.ui.screens.tutorials.ConceptSubGroup.entries.map { it.jsonKey }.toSet()
        assertEquals(enumJsonKeys, content.conceptSubgroupHeadings.keys)
        content.conceptSubgroupHeadings.forEach { (key, heading) ->
            assertTrue("$key title blank", heading.title.isNotBlank())
            assertTrue("$key subtitle blank", heading.subtitle.isNotBlank())
        }
    }

    @Test
    fun conceptDiagramOverridesLandOnExpectedArticleAndCard() {
        val content = loadContent()
        assertEquals("yield_curve", content.conceptArticles.getValue("macro_rates").keyIdeasDiagram)
        assertNull(content.conceptArticles.getValue("macro_rates").inMarketPulseDiagram)
        assertEquals("sma_extension", content.conceptArticles.getValue("technical_concepts").keyIdeasDiagram)
        assertEquals("sma_extension", content.conceptArticles.getValue("support_resistance").inMarketPulseDiagram)
        assertNull(content.conceptArticles.getValue("support_resistance").keyIdeasDiagram)
        assertEquals("yield_curve", content.conceptArticles.getValue("fed_policy_cycles").inMarketPulseDiagram)
        assertEquals("sentiment_gauges_live", content.conceptArticles.getValue("sentiment_positioning").inMarketPulseDiagram)
        assertEquals("vix_card_preview", content.conceptArticles.getValue("volatility_regimes").inMarketPulseDiagram)
        assertEquals("rate_sensitivity", content.conceptArticles.getValue("rate_sensitivity").keyIdeasDiagram)
        assertEquals("growth_vs_value", content.conceptArticles.getValue("growth_vs_value").keyIdeasDiagram)
        assertEquals("valuation_basics", content.conceptArticles.getValue("valuation_basics").keyIdeasDiagram)
        assertEquals("pe_expansion", content.conceptArticles.getValue("pe_expansion_contraction").keyIdeasDiagram)
        assertEquals("breadth", content.conceptArticles.getValue("breadth_divergence").keyIdeasDiagram)
        assertEquals("relative_strength", content.conceptArticles.getValue("relative_strength").keyIdeasDiagram)
        assertEquals("market_phases", content.conceptArticles.getValue("market_phases").keyIdeasDiagram)
        assertEquals("sector_rotation", content.conceptArticles.getValue("sector_rotation").keyIdeasDiagram)
        assertEquals("role_of_liquidity", content.conceptArticles.getValue("role_of_liquidity").inMarketPulseDiagram)
        assertEquals("correlation_breakdown", content.conceptArticles.getValue("correlation_breakdown").keyIdeasDiagram)
        // Every other article carries no diagram.
        val withDiagrams = setOf(
            "macro_rates", "technical_concepts", "support_resistance",
            "fed_policy_cycles", "sentiment_positioning", "volatility_regimes",
            "rate_sensitivity", "growth_vs_value", "valuation_basics", "pe_expansion_contraction",
            "breadth_divergence", "relative_strength", "market_phases", "sector_rotation",
            "role_of_liquidity", "correlation_breakdown"
        )
        content.conceptArticles.filterKeys { it !in withDiagrams }.forEach { (routeKey, article) ->
            assertNull("$routeKey unexpectedly has keyIdeasDiagram", article.keyIdeasDiagram)
            assertNull("$routeKey unexpectedly has inMarketPulseDiagram", article.inMarketPulseDiagram)
        }
    }

    @Test
    fun mechanismDeckDiagramOverridesLandOnExpectedDeck() {
        val content = loadContent()
        assertEquals("sentiment_gauges", content.mechanismDecks.getValue("tactical_momentum").theSignalsDiagram)
        assertEquals("yield_curve", content.mechanismDecks.getValue("systemic_risk").whatToWatchDiagram)
        assertEquals("posture_rings_preview", content.mechanismDecks.getValue("posture").whatToWatchDiagram)
        assertEquals("positioning_bars_preview", content.mechanismDecks.getValue("positioning").whatToWatchDiagram)
        assertEquals("macro_vitals", content.mechanismDecks.getValue("macro_vitals").howToReadTogetherDiagram)
        assertEquals("stock_analysis", content.mechanismDecks.getValue("stock_analysis").howToReadTogetherDiagram)
        assertEquals("positioning", content.mechanismDecks.getValue("positioning").howToReadTogetherDiagram)
        val withDiagrams = setOf("tactical_momentum", "systemic_risk", "posture", "positioning")
        content.mechanismDecks.filterKeys { it !in withDiagrams }.forEach { (routeKey, deck) ->
            assertNull("$routeKey unexpectedly has theSignalsDiagram", deck.theSignalsDiagram)
            assertNull("$routeKey unexpectedly has whatToWatchDiagram", deck.whatToWatchDiagram)
        }
        val withHowToReadTogetherDiagrams = setOf("macro_vitals", "stock_analysis", "positioning")
        content.mechanismDecks.filterKeys { it !in withHowToReadTogetherDiagrams }.forEach { (routeKey, deck) ->
            assertNull("$routeKey unexpectedly has howToReadTogetherDiagram", deck.howToReadTogetherDiagram)
        }
    }

    @Test
    fun gaugeAnatomyHas4CardsAndDataLimitationsHas1() {
        val content = loadContent()
        assertEquals("How to Read Any Gauge", content.gaugeAnatomy.title)
        assertEquals(4, content.gaugeAnatomy.cards.size)
        assertEquals("gauge_anatomy", content.gaugeAnatomy.cards.first().diagram)

        assertEquals("Data Limitations", content.dataLimitations.title)
        assertEquals(1, content.dataLimitations.cards.size)
    }

    @Test
    fun everyConceptArticleRouteKeyMatchesTheJsonFile() {
        // Cross-check against the Kotlin enum this JSON has to stay in sync with by hand (see
        // LearnContentJson's own doc comment) -- guards against a typo'd or renamed route key.
        val content = loadContent()
        val enumRouteKeys = com.marketlabs.pulse.ui.screens.tutorials.ConceptArticle.entries.map { it.routeKey }.toSet()
        assertEquals(enumRouteKeys, content.conceptArticles.keys)
    }

    @Test
    fun everyMechanismRouteKeyMatchesTheJsonFile() {
        val content = loadContent()
        val enumRouteKeys = com.marketlabs.pulse.ui.components.tutorials.Mechanism.entries.map { it.routeKey }.toSet()
        assertEquals(enumRouteKeys, content.mechanismDecks.keys)
    }

    // --- Diagram-key validation ------------------------------------------------------------
    // Moshi silently drops JSON keys it doesn't recognize, so a typo'd or renamed diagram field
    // (e.g. "what_this_is_diagram") would otherwise compile, parse, and just render nothing on
    // device. These walk the file as raw, untyped JSON instead of through LearnContentJson, so a
    // mistake shows up as a failing test rather than silently-missing content.

    private val conceptDiagramFields = setOf("key_ideas_diagram", "in_market_pulse_diagram")
    private val mechanismDiagramFields = setOf("the_signals_diagram", "what_to_watch_diagram", "how_to_read_together_diagram")
    private val standaloneDiagramField = "diagram"
    private val allKnownDiagramFields = conceptDiagramFields + mechanismDiagramFields + standaloneDiagramField

    private fun loadRawJson(): Map<String, Any?> {
        val file = File("src/main/assets/learn_content.json")
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        val adapter = Moshi.Builder().build().adapter<Map<String, Any?>>(type)
        return adapter.fromJson(file.readText())!!
    }

    @Test
    fun everyDiagramFieldNameIsRecognized() {
        val root = loadRawJson()

        @Suppress("UNCHECKED_CAST")
        fun walkConceptArticle(article: Map<String, Any?>) {
            article.keys.filter { it.endsWith("_diagram") }.forEach {
                assertTrue("Unrecognized concept-article diagram field \"$it\"", it in conceptDiagramFields)
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun walkMechanismDeck(deck: Map<String, Any?>) {
            deck.keys.filter { it.endsWith("_diagram") }.forEach {
                assertTrue("Unrecognized mechanism-deck diagram field \"$it\"", it in mechanismDiagramFields)
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun walkStandaloneArticle(article: Map<String, Any?>) {
            val cards = article["cards"] as List<Map<String, Any?>>
            cards.forEach { card ->
                card.keys.filter { it == standaloneDiagramField || it.endsWith("_diagram") }.forEach {
                    assertTrue("Unrecognized standalone-card diagram field \"$it\"", it == standaloneDiagramField)
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        val marketConcepts = root["market_concepts"] as Map<String, List<Map<String, Any?>>>
        marketConcepts.values.flatten().forEach(::walkConceptArticle)

        @Suppress("UNCHECKED_CAST")
        val marketMechanisms = root["market_mechanisms"] as Map<String, Map<String, Any?>>
        marketMechanisms.values.forEach(::walkMechanismDeck)

        @Suppress("UNCHECKED_CAST")
        walkStandaloneArticle(root["gauge_anatomy"] as Map<String, Any?>)
        @Suppress("UNCHECKED_CAST")
        walkStandaloneArticle(root["data_limitations"] as Map<String, Any?>)
    }

    @Test
    fun everyDiagramValueIsAKnownDiagramKey() {
        val root = loadRawJson()
        val foundValues = mutableListOf<String>()

        @Suppress("UNCHECKED_CAST")
        val marketConcepts = root["market_concepts"] as Map<String, List<Map<String, Any?>>>
        marketConcepts.values.flatten().forEach { article ->
            allKnownDiagramFields.forEach { field -> (article[field] as? String)?.let(foundValues::add) }
        }

        @Suppress("UNCHECKED_CAST")
        val marketMechanisms = root["market_mechanisms"] as Map<String, Map<String, Any?>>
        marketMechanisms.values.forEach { deck ->
            allKnownDiagramFields.forEach { field -> (deck[field] as? String)?.let(foundValues::add) }
        }

        listOf(root["gauge_anatomy"], root["data_limitations"]).forEach { article ->
            @Suppress("UNCHECKED_CAST")
            val cards = (article as Map<String, Any?>)["cards"] as List<Map<String, Any?>>
            cards.forEach { card -> (card[standaloneDiagramField] as? String)?.let(foundValues::add) }
        }

        assertTrue("No diagram values found -- test is not exercising anything", foundValues.isNotEmpty())
        foundValues.forEach { value ->
            assertTrue("\"$value\" is not in KNOWN_DIAGRAM_KEYS", value in KNOWN_DIAGRAM_KEYS)
        }
    }

    @Test
    fun everyKnownDiagramKeyResolvesToARealComposable() {
        KNOWN_DIAGRAM_KEYS.forEach { key ->
            assertNotNull("learnDiagramFor(\"$key\") returned null", learnDiagramFor(key))
        }
    }

    // --- Route-chip validation ---------------------------------------------------------------
    // Same rationale as the diagram-key checks above: Moshi silently drops an unrecognized JSON
    // key, and `PulseRoutes` is a flat bag of `const val` strings Moshi can't validate against
    // structurally, so a typo'd field name or a typo'd route constant would otherwise compile,
    // parse, and just produce a chip that goes nowhere.

    private val conceptRouteFields = setOf("in_market_pulse_routes")
    private val mechanismRouteFields = setOf("the_signals_routes", "what_to_watch_routes", "how_to_read_together_routes")

    @Test
    fun everyRouteFieldNameIsRecognized() {
        val root = loadRawJson()

        @Suppress("UNCHECKED_CAST")
        val marketConcepts = root["market_concepts"] as Map<String, List<Map<String, Any?>>>
        marketConcepts.values.flatten().forEach { article ->
            article.keys.filter { it.endsWith("_routes") }.forEach {
                assertTrue("Unrecognized concept-article route field \"$it\"", it in conceptRouteFields)
            }
        }

        @Suppress("UNCHECKED_CAST")
        val marketMechanisms = root["market_mechanisms"] as Map<String, Map<String, Any?>>
        marketMechanisms.values.forEach { deck ->
            deck.keys.filter { it.endsWith("_routes") }.forEach {
                assertTrue("Unrecognized mechanism-deck route field \"$it\"", it in mechanismRouteFields)
            }
        }
    }

    @Test
    fun everyRouteChipTargetIsARealPulseRoutesConstant() {
        val knownRouteNames = com.marketlabs.pulse.ui.navigation.PulseRoutes::class.java.declaredFields
            .filter { it.type == String::class.java }
            .map { it.name }
            .toSet()
        assertTrue("Reflection found no PulseRoutes constants -- test is not exercising anything", knownRouteNames.isNotEmpty())

        val content = loadContent()
        val foundChips = mutableListOf<LearnRouteChip>()
        content.conceptArticles.values.forEach { article -> article.inMarketPulseRoutes?.let(foundChips::addAll) }
        content.mechanismDecks.values.forEach { deck ->
            deck.theSignalsRoutes?.let(foundChips::addAll)
            deck.whatToWatchRoutes?.let(foundChips::addAll)
            deck.howToReadTogetherRoutes?.let(foundChips::addAll)
        }

        assertTrue("No route chips found -- test is not exercising anything", foundChips.isNotEmpty())
        foundChips.forEach { chip ->
            assertTrue("\"${chip.route}\" (label \"${chip.label}\") is not a PulseRoutes constant", chip.route in knownRouteNames)
            assertTrue("\"${chip.label}\" chip label is blank", chip.label.isNotBlank())
        }
    }
}
