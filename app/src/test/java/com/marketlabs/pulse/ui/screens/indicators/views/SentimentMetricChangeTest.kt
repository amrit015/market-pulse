package com.marketlabs.pulse.ui.screens.indicators.views

import com.marketlabs.pulse.storage.model.indicators.DomainUnifiedMetric
import com.marketlabs.pulse.ui.components.widgets.ChangeDirection
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.utils.enums.SignalColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SentimentMetricChangeTest {

    private val colors = MarketPulseTheme.NAVY.toPulseColors()

    private fun metric(id: String, value: Double?, previous: Double?, change: Double? = null) = DomainUnifiedMetric(
        id = id, name = id, category = "TACTICAL_MOMENTUM", subcategory = null,
        valueRaw = value, valueDisplay = value?.toString(),
        previousValueRaw = previous, previousValueDisplay = previous?.toString(),
        changeRaw = change, changeDisplay = null,
        signalText = null, signalColor = SignalColor.UNKNOWN, releaseDate = null
    )

    @Test
    fun fearAndGreedRisingIsAnUpTriangleOnABearishPillWithIntegerPoints() {
        val result = sentimentChangeFor(metric("fear_and_greed", 31.0, 27.0), colors)!!

        assertEquals("4", result.text)
        assertEquals(ChangeDirection.UP, result.direction)
        assertEquals(colors.signalBearishPill, result.pillColor)
        assertEquals(colors.signalBearishText, result.contentColor)
    }

    @Test
    fun fearAndGreedFallingIsADownTriangleOnABullishPill() {
        val result = sentimentChangeFor(metric("fear_and_greed", 25.0, 29.0), colors)!!

        assertEquals(ChangeDirection.DOWN, result.direction)
        assertEquals(colors.signalBullishPill, result.pillColor)
    }

    @Test
    fun putCallRisingIsBullishWithTwoDecimalPercent() {
        val result = sentimentChangeFor(metric("put_call_ratio", 20.0, 16.0), colors)!!

        assertEquals("25.00%", result.text)
        assertEquals(ChangeDirection.UP, result.direction)
        assertEquals(colors.signalBullishPill, result.pillColor)
    }

    @Test
    fun vixOnIndicatorsRisingIsBearishBecausePanicIsRedThere() {
        val rising = sentimentChangeFor(metric("vix", 20.0, 16.0), colors)!!
        assertEquals("25.00%", rising.text)
        assertEquals(ChangeDirection.UP, rising.direction)
        assertEquals(colors.signalBearishPill, rising.pillColor)

        val falling = sentimentChangeFor(metric("vix", 15.0, 20.0), colors)!!
        assertEquals(ChangeDirection.DOWN, falling.direction)
        assertEquals(colors.signalBullishPill, falling.pillColor)
    }

    @Test
    fun putCallFallingIsABearishPill() {
        val result = sentimentChangeFor(metric("put_call_ratio", 15.0, 20.0), colors)!!

        assertEquals(ChangeDirection.DOWN, result.direction)
        assertEquals(colors.signalBearishPill, result.pillColor)
    }

    @Test
    fun noMoveIsNeutralAndFlat() {
        val result = sentimentChangeFor(metric("fear_and_greed", 40.0, 40.0), colors)!!

        assertEquals(ChangeDirection.FLAT, result.direction)
        assertEquals(colors.signalNeutralPill, result.pillColor)
    }

    @Test
    fun otherMetricsAndUncomputableMovesFallBackToTheGenericPill() {
        assertNull(sentimentChangeFor(metric("cpi_yoy", 3.0, 2.0), colors))
        assertNull(sentimentChangeFor(metric("vix", null, null), colors))
    }
}
