package com.marketlabs.pulse.core.learn

import com.marketlabs.pulse.ui.screens.tutorials.TutorialsGaugesCatalog
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Parses the real `assets/indicator_articles.json` (plain JVM test, no Robolectric/instrumentation
 * in this project) with the same Moshi adapter [IndicatorArticlesProvider] uses at runtime, and
 * cross-checks its 35 indicator ids against [TutorialsGaugesCatalog]'s own inventory -- the same
 * "JSON must stay in sync with the Kotlin catalog it's rendered alongside" check
 * `LearnContentJsonTest.everyMechanismRouteKeyMatchesTheJsonFile` does for mechanism decks.
 */
class IndicatorArticlesJsonTest {

    private val eventOnlyKeys = setOf(
        "ism_manufacturing_pmi", "ism_services_pmi", "ppi", "initial_jobless_claims", "fomc_rate_decision"
    )

    private fun loadArticles(): Map<String, IndicatorArticle> {
        val file = File("src/main/assets/indicator_articles.json")
        assertTrue("indicator_articles.json not found at ${file.absolutePath}", file.exists())
        val json = file.readText()
        val type = Types.newParameterizedType(Map::class.java, String::class.java, IndicatorArticle::class.java)
        val parsed = Moshi.Builder().build().adapter<Map<String, IndicatorArticle>>(type).fromJson(json)
        assertNotNull("Moshi failed to parse indicator_articles.json", parsed)
        return parsed!!
    }

    @Test
    fun fileHas40ArticlesWithNonBlankContentAndFiveCardsEach() {
        val articles = loadArticles()
        assertEquals(40, articles.size)
        articles.forEach { (key, article) ->
            assertTrue("$key title blank", article.title.isNotBlank())
            assertTrue("$key subtitle blank", article.subtitle.isNotBlank())
            assertEquals("$key does not have 5 cards", 5, article.cards.size)
            article.cards.forEach { card ->
                assertTrue("$key card title blank", card.title.isNotBlank())
                assertTrue("$key/${card.title} body blank", card.body.isNotBlank())
                assertTrue("$key/${card.title} still has a markdown bullet", !card.body.contains("\n- "))
                assertTrue("$key/${card.title} still has a markdown bullet", !card.body.startsWith("- "))
            }
        }
    }

    @Test
    fun indicatorKeysMatchTutorialsGaugesCatalogExactly() {
        val articles = loadArticles()
        val indicatorKeys = articles.keys - eventOnlyKeys
        val catalogKeys = TutorialsGaugesCatalog.categories.flatMap { it.metricIds }.toSet()
        assertEquals(catalogKeys, indicatorKeys)
    }

    @Test
    fun eventOnlyKeysAreExactlyTheFiveWithNoGauge() {
        val articles = loadArticles()
        assertTrue(
            "Expected event-only keys $eventOnlyKeys to all be present",
            articles.keys.containsAll(eventOnlyKeys)
        )
    }
}
