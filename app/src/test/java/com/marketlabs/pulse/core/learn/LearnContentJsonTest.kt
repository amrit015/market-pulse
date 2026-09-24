package com.marketlabs.pulse.core.learn

import com.squareup.moshi.Moshi
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
            assertTrue("$routeKey whatThisIs blank", deck.whatThisIs.isNotBlank())
            assertTrue("$routeKey theSignals blank", deck.theSignals.isNotBlank())
            assertTrue("$routeKey howToReadTogether blank", deck.howToReadTogether.isNotBlank())
            assertTrue("$routeKey whatToWatch blank", deck.whatToWatch.isNotBlank())
            assertTrue("$routeKey commonMisreads blank", deck.commonMisreads.isNotBlank())
            assertTrue("$routeKey inTheWild blank", deck.inTheWild.isNotBlank())
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
        // Every other article carries no diagram.
        val withDiagrams = setOf("macro_rates", "technical_concepts", "support_resistance")
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
        val withDiagrams = setOf("tactical_momentum", "systemic_risk")
        content.mechanismDecks.filterKeys { it !in withDiagrams }.forEach { (routeKey, deck) ->
            assertNull("$routeKey unexpectedly has theSignalsDiagram", deck.theSignalsDiagram)
            assertNull("$routeKey unexpectedly has whatToWatchDiagram", deck.whatToWatchDiagram)
        }
    }

    @Test
    fun gaugeAnatomyHas3CardsAndDataLimitationsHas1() {
        val content = loadContent()
        assertEquals("How to Read Any Gauge", content.gaugeAnatomy.title)
        assertEquals(3, content.gaugeAnatomy.cards.size)
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
}
