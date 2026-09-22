package com.marketlabs.pulse.ui.navigation

import org.junit.Assert.assertEquals
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
}
