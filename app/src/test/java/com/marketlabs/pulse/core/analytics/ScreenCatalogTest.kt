package com.marketlabs.pulse.core.analytics

import com.marketlabs.pulse.ui.navigation.PulseRoutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenCatalogTest {

    // Not navigable destinations: a lookup helper, never passed to composable().
    private val nonDestinationConstants = emptySet<String>()

    @Test
    fun everyRouteConstantHasAScreenName() {
        val routeConstants = PulseRoutes::class.java.declaredFields
            .filter { java.lang.reflect.Modifier.isStatic(it.modifiers) && it.type == String::class.java }
            .map { it.name }
            .filterNot { it == "INSTANCE" || it in nonDestinationConstants }
        val covered = ScreenName.entries.map { it.screenClass }.toSet()
        val missing = routeConstants.filterNot { it in covered }
        assertTrue("PulseRoutes constants without a ScreenName: $missing", missing.isEmpty())
    }

    @Test
    fun screenNamesAreUniqueAndWellFormed() {
        val values = ScreenName.entries.map { it.value }
        assertEquals(values.size, values.toSet().size)
        values.forEach { assertTrue("bad screen name: $it", Regex("^[a-z][a-z0-9_]{0,39}$").matches(it)) }
    }

    @Test
    fun tabNamesAreBaseNamePlusLowerSnakeTab() {
        assertEquals("indicators_tactical_momentum", ScreenName.INDICATORS.forTab(Tab.TACTICAL_MOMENTUM).name)
        assertEquals("MARKET_INDICATORS", ScreenName.INDICATORS.forTab(Tab.TACTICAL_MOMENTUM).screenClass)
    }

    private enum class Tab { TACTICAL_MOMENTUM }
}
