package com.marketlabs.pulse.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsEventContractTest {

    private val samples: List<AnalyticsEvent> = listOf(
        AnalyticsEvent.ContentOpen(AnalyticsEvent.ContentType.STOCK, "AAPL", AnalyticsEvent.Source.LIST),
        AnalyticsEvent.SheetOpen(AnalyticsEvent.Sheet.RISK, "insights_risks"),
        AnalyticsEvent.SheetOpen(AnalyticsEvent.Sheet.RISK, "insights_risks", itemId = "x"),
        AnalyticsEvent.TutorialBegin(AnalyticsEvent.TutorialKey.Article("cpi_yoy")),
        AnalyticsEvent.TutorialBegin(AnalyticsEvent.TutorialKey.Deck("momentum")),
        AnalyticsEvent.TutorialComplete(AnalyticsEvent.TutorialKey.Article("cpi_yoy")),
        AnalyticsEvent.PushOpen("summary", "k", "market_summary", AnalyticsEvent.Launch.COLD),
        AnalyticsEvent.PushOpen(null, null, "market_overview", AnalyticsEvent.Launch.WARM),
        AnalyticsEvent.NewsOutbound("reuters.com", 3L),
        AnalyticsEvent.ChartRangeChange("ONE_MONTH", "stock_detail"),
        AnalyticsEvent.PullToRefresh("news"),
        AnalyticsEvent.DataStateShown(AnalyticsEvent.DataState.ERROR, "news", "market_news"),
        AnalyticsEvent.FavoriteToggle(AnalyticsEvent.FavoriteKind.STOCK, "AAPL", on = true),
        AnalyticsEvent.ThemeChange("navy"),
        AnalyticsEvent.OnboardingStep(2L, AnalyticsEvent.OnboardingAction.NEXT),
        AnalyticsEvent.LegalAccept(2L)
    )

    private val nameRegex = Regex("^[a-z][a-z0-9_]{0,39}$")
    private val reservedPrefixes = listOf("firebase_", "google_", "ga_")

    @Test
    fun everyEventSubclassIsCoveredBySamples() {
        val covered = samples.map { it::class }.toSet()
        assertEquals(AnalyticsEvent::class.sealedSubclasses.toSet(), covered)
    }

    @Test
    fun eventNamesFollowFirebaseRules() {
        samples.forEach { event ->
            assertTrue("bad event name: ${event.name}", nameRegex.matches(event.name))
            assertTrue(reservedPrefixes.none { event.name.startsWith(it) })
        }
    }

    @Test
    fun paramsFollowFirebaseRules() {
        samples.forEach { event ->
            assertTrue("too many params: ${event.name}", event.params.size <= 25)
            event.params.forEach { (key, value) ->
                assertTrue("bad param name: $key", nameRegex.matches(key))
                assertTrue(reservedPrefixes.none { key.startsWith(it) })
                assertTrue("param $key must be String or Long", value is String || value is Long)
                if (value is String) assertTrue("param $key too long", value.length <= 100)
            }
        }
    }

    @Test
    fun absentPushKeyIsOmittedNotEmpty() {
        val params = AnalyticsEvent.PushOpen("summary", null, "market_summary", AnalyticsEvent.Launch.COLD).params
        assertTrue("push_key" !in params)
    }

    @Test
    fun favoritesBucketsAreCoarse() {
        assertEquals("0", UserProperty.favoritesBucket(0))
        assertEquals("1_3", UserProperty.favoritesBucket(3))
        assertEquals("4_10", UserProperty.favoritesBucket(4))
        assertEquals("4_10", UserProperty.favoritesBucket(10))
        assertEquals("10_plus", UserProperty.favoritesBucket(11))
    }
}
