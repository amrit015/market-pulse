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
        AnalyticsEvent.LegalAccept(2L),
        AnalyticsEvent.SectionExpand(AnalyticsEvent.ExpandedSection.MARKET_MOVERS, "summary_drivers", 2L, 0L),
        AnalyticsEvent.SummaryExpandMode(expanded = true),
        AnalyticsEvent.DaySelect(3L)
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
    fun summaryExpandEventsCarryOnlyEnumeratedValuesAndOffsets() {
        val expand = AnalyticsEvent.SectionExpand(AnalyticsEvent.ExpandedSection.MACRO_MIX, "summary_drivers", 1L, 2L)
        assertEquals("section_expand", expand.name)
        assertEquals(
            mapOf<String, Any>(
                "screen_name" to "summary_drivers",
                "section" to "macro_mix",
                "item_position" to 1L,
                "day_offset" to 2L
            ),
            expand.params
        )
        assertEquals("expanded", AnalyticsEvent.SummaryExpandMode(true).params["mode"])
        assertEquals("collapsed", AnalyticsEvent.SummaryExpandMode(false).params["mode"])
        assertEquals(mapOf<String, Any>("day_offset" to 0L), AnalyticsEvent.DaySelect(0L).params)
    }

    @Test
    fun marketReadIsNoLongerASheet() {
        assertTrue(AnalyticsEvent.Sheet.entries.none { it.value == "market_read" })
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
