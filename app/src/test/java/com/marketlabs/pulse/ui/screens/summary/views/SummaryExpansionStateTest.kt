package com.marketlabs.pulse.ui.screens.summary.views

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SummaryExpansionStateTest {

    @Test
    fun rowsFollowTheDefaultUntilTapped() {
        val state = SummaryExpansionState()

        assertFalse(state.isExpanded(SummarySection.MARKET_MOVERS, 0, rowDefault = false))
        assertTrue(state.isExpanded(SummarySection.MARKET_MOVERS, 0, rowDefault = true))
    }

    @Test
    fun aTapOverridesTheDefaultForThatRowOnly() {
        val state = SummaryExpansionState()

        val expandedNow = state.toggle(SummarySection.MARKET_MOVERS, 1, rowDefault = false)

        assertTrue(expandedNow)
        assertTrue(state.isExpanded(SummarySection.MARKET_MOVERS, 1, rowDefault = false))
        assertFalse(state.isExpanded(SummarySection.MARKET_MOVERS, 0, rowDefault = false))
        assertFalse(state.isExpanded(SummarySection.MACRO_MIX, 1, rowDefault = false))
    }

    @Test
    fun tappingAnExpandedByDefaultRowCollapsesIt() {
        val state = SummaryExpansionState()

        val expandedNow = state.toggle(SummarySection.MACRO_MIX, 0, rowDefault = true)

        assertFalse(expandedNow)
        assertFalse(state.isExpanded(SummarySection.MACRO_MIX, 0, rowDefault = true))
    }

    @Test
    fun nonRowSectionsIgnoreTheDefault() {
        val state = SummaryExpansionState()

        assertFalse(state.isExpanded(SummarySection.MARKET_READ, 0, rowDefault = true))
        assertFalse(state.isExpanded(SummarySection.MARKET_SENTIMENT, 0, rowDefault = true))
        assertFalse(state.isExpanded(SummarySection.WHAT_CHANGED, 0, rowDefault = true))
    }

    @Test
    fun clearingRowOverridesLeavesNonRowSectionsAlone() {
        val state = SummaryExpansionState()
        state.toggle(SummarySection.MARKET_MOVERS, 0, rowDefault = false)
        state.toggle(SummarySection.MARKET_READ, 0, rowDefault = false)
        state.toggle(SummarySection.MARKET_SENTIMENT, 0, rowDefault = false)

        state.clearRowOverrides()

        assertFalse(state.isExpanded(SummarySection.MARKET_MOVERS, 0, rowDefault = false))
        assertTrue(state.isExpanded(SummarySection.MARKET_READ, 0, rowDefault = false))
        assertTrue(state.isExpanded(SummarySection.MARKET_SENTIMENT, 0, rowDefault = false))
    }

    @Test
    fun onlyTheOutlookTabCanBeLocked() {
        assertEquals(listOf(SummaryTab.OUTLOOK), SummaryTab.entries.filter { it.lockedBy != null })
    }

    @Test
    fun v1AccessAllowsEveryFeature() {
        val access = SummaryAccess(isPro = true)
        assertTrue(SummaryFeature.entries.all(access::allows))
    }
}
