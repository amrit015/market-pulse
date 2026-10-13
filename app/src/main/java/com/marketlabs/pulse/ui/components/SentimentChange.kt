package com.marketlabs.pulse.ui.components

import androidx.compose.ui.graphics.Color
import com.marketlabs.pulse.ui.components.widgets.ChangeDirection
import com.marketlabs.pulse.ui.theme.PulseColors
import kotlin.math.abs

/**
 * A pre-resolved change pill: text (already unsigned), triangle direction, and the two colors.
 * Used where the pill color is not simply the metric's signal color -- the sentiment readings
 * color the pill by what the move means for the market, independent of the triangle's raw sign.
 */
data class MetricChangeDisplay(
    val text: String,
    val direction: ChangeDirection,
    val pillColor: Color,
    val contentColor: Color
)

/**
 * The one place the sentiment readings' change pill is decided, so Overview's cards, the Indicators
 * cards, and the asset detail page can never disagree about a reading:
 *
 * - The triangle follows the raw sign of the move.
 * - The pill color is the contrarian market read, which may disagree with the triangle.
 * - Text is unsigned (the triangle states the sign). An exact zero is a neutral flat bar.
 */
object SentimentChange {

    /**
     * Fear & Greed: integer point change. Rising (toward Greed) reads bearish, falling (toward
     * Fear) reads bullish. Null when there is no previous score to compare with.
     */
    fun fearGreed(score: Double, previousScore: Double?, colors: PulseColors): MetricChangeDisplay? {
        if (previousScore == null) return null
        val delta = score - previousScore
        return resolve(
            delta = delta,
            text = abs(delta).toInt().toString(),
            risingIsBullish = false,
            colors = colors
        )
    }

    /**
     * VIX and Put/Call: percent change to two decimals. By default rising (more fear / more
     * hedging) reads bullish, falling reads bearish -- Overview's contrarian read. Indicators' VIX
     * card passes `risingIsBullish = false`: it classifies VIX as Calm/Elevated/Panic with Panic
     * red, so a rising VIX has to read red there to agree with its own status pill.
     */
    fun percent(
        changePercent: Double,
        colors: PulseColors,
        risingIsBullish: Boolean = true
    ): MetricChangeDisplay = resolve(
        delta = changePercent,
        text = "${String.format("%.2f", abs(changePercent))}%",
        risingIsBullish = risingIsBullish,
        colors = colors
    )

    private fun resolve(
        delta: Double,
        text: String,
        risingIsBullish: Boolean,
        colors: PulseColors
    ): MetricChangeDisplay {
        if (delta == 0.0) {
            return MetricChangeDisplay(text, ChangeDirection.FLAT, colors.signalNeutralPill, colors.signalNeutralText)
        }
        val isRising = delta > 0
        val isBullish = if (risingIsBullish) isRising else !isRising
        return MetricChangeDisplay(
            text = text,
            direction = if (isRising) ChangeDirection.UP else ChangeDirection.DOWN,
            pillColor = if (isBullish) colors.signalBullishPill else colors.signalBearishPill,
            contentColor = if (isBullish) colors.signalBullishText else colors.signalBearishText
        )
    }
}
