package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.storage.model.intraday.IntradayPoint
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * The intraday "today" sparkline — backend-polled bars from `IntradayRepository` (`GET
 * /intraday/:symbol`), colored bullish/bearish against `previousClose` (the standard
 * is-today-green-or-red comparison against the prior trading day's close), with a dotted flat
 * line at that same baseline spanning the full width and a pulsing dot marking the latest point.
 * A plain Canvas widget, not Vico — [PeriodChart][com.marketlabs.pulse.ui.components.charts.PeriodChart]
 * is the heavier, interactive chart; this is deliberately simple and cheap to redraw.
 *
 * Real bars are spaced evenly across the *full* width, always -- whatever the trading-hours gate
 * on the backend already trimmed `points` down to (9:30am-4:00pm ET for equities/sectors/gold/
 * silver/oil/copper, all day for crypto) is exactly the data this draws, and it fills the card
 * edge to edge whether the session is still live or finished hours ago. There's no separate
 * "elapsed portion of the day" concept on the client -- a completed session's line shouldn't look
 * partially empty just because you're viewing it after hours, and a live, in-progress session
 * still reads fine stretched to fit what's arrived so far (each new bar simply reflows the
 * existing ones instead of leaving unfilled space to the right).
 *
 * Draws nothing beyond the dashed baseline when `points` has fewer than 2 entries (no bars polled
 * yet, or a stale prior-day doc filtered out upstream) -- a synthesized flat line would look like
 * real (flat) price action rather than "no data yet," which is misleading, so this deliberately
 * shows nothing instead.
 */
@Composable
fun SparklineChart(
    points: List<IntradayPoint>,
    previousClose: Double?,
    modifier: Modifier = Modifier
) {
    val pulseColors = LocalPulseColors.current

    val prices = if (points.isNotEmpty()) points.map { it.price } else listOfNotNull(previousClose)
    if (prices.isEmpty()) return

    val latestPrice = prices.last()
    val isBullish = previousClose == null || latestPrice >= previousClose
    val lineColor = if (isBullish) pulseColors.signalBullishText else pulseColors.signalBearishText

    val infiniteTransition = rememberInfiniteTransition(label = "sparkline_pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1400), repeatMode = RepeatMode.Restart),
        label = "sparkline_pulse_progress"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val allValues = if (previousClose != null) prices + previousClose else prices
        val minPrice = allValues.min()
        val maxPrice = allValues.max()
        val valueRange = (maxPrice - minPrice).let { if (it > 0.0) it else 1.0 }
        // Headroom above/below the data so a peak/trough never touches the card's edge.
        val verticalPadding = valueRange * 0.2
        val paddedMin = minPrice - verticalPadding
        val paddedRange = valueRange + verticalPadding * 2

        fun yFor(price: Double): Float = height - ((price - paddedMin) / paddedRange * height).toFloat()

        previousClose?.let {
            drawLine(
                color = lineColor.copy(alpha = 0.5f),
                start = Offset(0f, yFor(it)),
                end = Offset(width, yFor(it)),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
            )
        }

        // No real bars yet -- nothing beyond the dashed baseline above. See the doc comment on
        // why this stays blank rather than drawing a synthesized flat line.
        if (points.size < 2) return@Canvas

        val stepX = width / (points.size - 1)
        val offsets = prices.mapIndexed { index, price -> Offset(index * stepX, yFor(price)) }
        val linePath = smoothPath(offsets)

        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(offsets.last().x, height)
            lineTo(offsets.first().x, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0.02f)))
        )
        drawPath(path = linePath, color = lineColor, style = Stroke(width = 2.dp.toPx()))

        drawPulsingDot(center = offsets.last(), color = lineColor, pulseProgress = pulseProgress)
    }
}

/** Smooths a polyline into cubic-Bézier segments (a control point at each pair's horizontal midpoint). */
private fun smoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    for (i in 0 until points.size - 1) {
        val current = points[i]
        val next = points[i + 1]
        val midX = (current.x + next.x) / 2f
        path.cubicTo(midX, current.y, midX, next.y, next.x, next.y)
    }
    return path
}

private fun DrawScope.drawPulsingDot(
    center: Offset,
    color: Color,
    pulseProgress: Float
) {
    drawCircle(
        color = color.copy(alpha = (1f - pulseProgress) * 0.5f),
        radius = 3.dp.toPx() + (5.dp.toPx() * pulseProgress),
        center = center
    )
    drawCircle(color = color, radius = 3.dp.toPx(), center = center)
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val mockRisingPoints = listOf(
    IntradayPoint(570, 100.0), IntradayPoint(575, 101.5), IntradayPoint(580, 100.8),
    IntradayPoint(585, 102.4), IntradayPoint(590, 103.1), IntradayPoint(595, 102.7),
    IntradayPoint(600, 104.0)
)

private val mockFallingPoints = listOf(
    IntradayPoint(570, 104.0), IntradayPoint(575, 102.7), IntradayPoint(580, 103.1),
    IntradayPoint(585, 102.4), IntradayPoint(590, 100.8), IntradayPoint(595, 101.5),
    IntradayPoint(600, 100.0)
)

@Preview(name = "Rising - Light", showBackground = true)
@Composable
private fun PreviewSparklineChartRisingLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        SparklineChart(
            points = mockRisingPoints,
            previousClose = 101.0,
            modifier = Modifier.width(120.dp).height(32.dp)
        )
    }
}

@Preview(name = "Falling - Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewSparklineChartFallingDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        SparklineChart(
            points = mockFallingPoints,
            previousClose = 103.0,
            modifier = Modifier.width(120.dp).height(32.dp)
        )
    }
}

@Preview(name = "Empty (flat seed)", showBackground = true)
@Composable
private fun PreviewSparklineChartEmpty() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        SparklineChart(
            points = emptyList(),
            previousClose = 101.0,
            modifier = Modifier.width(120.dp).height(32.dp)
        )
    }
}
