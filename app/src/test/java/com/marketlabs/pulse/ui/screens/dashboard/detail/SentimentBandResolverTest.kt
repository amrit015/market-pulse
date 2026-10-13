package com.marketlabs.pulse.ui.screens.dashboard.detail

import com.marketlabs.pulse.core.glossary.MetricGlossaryBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SentimentBandResolverTest {

    private val fiveBands = listOf("Extreme Fear", "Fear", "Neutral", "Greed", "Extreme Greed")
        .map { MetricGlossaryBand(it, "") }

    @Test
    fun statusMatchesBandLabelIgnoringCaseAndWhitespace() {
        assertEquals(1, currentSentimentBandIndex("FEAR", fiveBands))
        assertEquals(4, currentSentimentBandIndex(" extreme greed ", fiveBands))
    }

    @Test
    fun unknownOrMissingStatusHighlightsNothing() {
        assertNull(currentSentimentBandIndex("WEIRD", fiveBands))
        assertNull(currentSentimentBandIndex(null, fiveBands))
    }

    @Test
    fun bandsAreOrderedExtremeGreedToExtremeFear() {
        val sorted = fiveBands.sortedBy { greedToFearOrder(it.label) }.map { it.label }

        assertEquals(listOf("Extreme Greed", "Greed", "Neutral", "Fear", "Extreme Fear"), sorted)
    }
}
