package com.marketlabs.pulse.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PushNavigationTest {

    @Test
    fun marketSummaryScreenOpensSummaryTab() {
        assertEquals(PulseRoutes.MARKET_SUMMARY, PushNavigation.routeForScreen("market_summary"))
    }

    @Test
    fun stocksScreenOpensAnalysisTab() {
        assertEquals(PulseRoutes.MARKET_ANALYSIS, PushNavigation.routeForScreen("stocks"))
    }

    @Test
    fun unknownOrMissingScreenOpensHome() {
        assertEquals(PulseRoutes.MARKET_OVERVIEW, PushNavigation.routeForScreen("something_new"))
        assertEquals(PulseRoutes.MARKET_OVERVIEW, PushNavigation.routeForScreen(null))
        assertEquals(PulseRoutes.MARKET_OVERVIEW, PushNavigation.routeForScreen(""))
    }

    @Test
    fun pushInfoCarriesTypeScreenAndKey() {
        val info = PushNavigation.pushInfoOf(isPush = true, type = "summary", screen = "market_summary", key = "2026-10-02")
        assertEquals(PushInfo(type = "summary", screen = "market_summary", key = "2026-10-02"), info)
    }

    @Test
    fun olderPushWithoutKeyHasNullKeyNotEmpty() {
        assertNull(PushNavigation.pushInfoOf(isPush = true, type = "summary", screen = null, key = null)?.key)
        assertNull(PushNavigation.pushInfoOf(isPush = true, type = "summary", screen = null, key = "")?.key)
    }

    @Test
    fun ordinaryLauncherIntentIsNotAPush() {
        assertNull(PushNavigation.pushInfoOf(isPush = false, type = null, screen = null, key = null))
    }
}
