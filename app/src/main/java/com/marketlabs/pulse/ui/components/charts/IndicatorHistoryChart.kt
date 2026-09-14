package com.marketlabs.pulse.ui.components.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.MetricHistoryPoint
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.utils.enums.SignalColor
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import kotlin.math.roundToInt

/**
 * History chart for one indicator metric, on the pushed metric-detail page -- shares [PeriodChart]'s
 * loading/empty/data state handling and [VicoLinePlot] host, but is a deliberately separate
 * composable rather than a third data-shape branch inside [PeriodChart]: an indicator reading
 * isn't a price. [PeriodChart]'s `$#,##0.00`-formatted caption/marker and first-vs-last
 * bullish/bearish coloring would both be wrong here -- a metric's `value` has no fixed unit (a raw
 * percent, ratio, index level...), so the marker and caption use each point's own pre-formatted
 * `valueDisplay` string instead of a client-derived number format. The line itself is always the
 * app's own accent color, not a bullish/bearish or [SignalColor]-tinted read -- indicators run on
 * their own up/down logic that doesn't correlate with green-is-good/red-is-bad the way a price
 * does (a rising unemployment rate going "up" isn't bullish/green the way a rising stock price
 * is), so a direction-colored line here would misstate the reading either way it was derived.
 *
 * [isStepLine], computed by the caller from its own domain's cadence rules (Indicators'
 * `MetricHistoryPillar.isMacroCadence`, Posture/Positioning's `InsightsHistoryPillar.isSparseCadence`
 * -- two different, non-overlapping id sets, which is why this composable takes the already-decided
 * boolean rather than a `metricId` and a lookup baked in here), picks a step-after line over the
 * usual smoothed curve for metrics whose points are weeks or months apart -- a curve between two
 * such points implies a trend that isn't real data. A single-point series (a metric with exactly one
 * recorded reading so far) renders as [SingleHistoryPointDisplay] instead of an empty-looking plot --
 * [VicoLinePlot] needs at least two points to draw a line at all.
 *
 * No range picker of its own -- backend history is a flat, most-recent-N series with no window
 * selection to expose at the chart level; callers that DO offer range switching (`MetricDetailScreen`,
 * `GlossaryDetailScreen`) already slice `points` down to the selected range before handing it here.
 */
@Composable
fun IndicatorHistoryChart(
    points: List<MetricHistoryPoint>,
    isStepLine: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val pulseColors = LocalPulseColors.current
    val lineColor = pulseColors.accentPrimary

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.stock_detail_chart_reserved_height)),
            contentAlignment = Alignment.Center
        ) {
            when {
                points.size > 1 -> IndicatorHistoryChartPlot(
                    points = points,
                    lineColor = lineColor,
                    isStepLine = isStepLine,
                    modifier = Modifier.fillMaxSize()
                )
                points.size == 1 -> SingleHistoryPointDisplay(point = points.first(), color = lineColor)
                isLoading -> CircularProgressIndicator()
                else -> Text(
                    text = stringResource(id = R.string.stock_detail_chart_empty_state),
                    style = MaterialTheme.typography.bodyMedium,
                    color = pulseColors.onSurfaceMuted
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.padding_xxlarge)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Each point's own valueDisplay, not a client-formatted number -- same reasoning as
            // the marker below. Only meaningful once there are two distinct endpoints to caption --
            // the single-point case already shows its one value inside SingleHistoryPointDisplay.
            if (points.size > 1) {
                Text(
                    text = points.first().valueDisplay ?: points.first().value.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = lineColor
                )
                Text(
                    text = points.last().valueDisplay ?: points.last().value.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = lineColor
                )
            }
        }
    }
}

/** Builds this chart's date-labeled x-axis/marker text (using each point's own `valueDisplay`) and delegates to [VicoLinePlot]. */
@Composable
private fun IndicatorHistoryChartPlot(
    points: List<MetricHistoryPoint>,
    lineColor: Color,
    isStepLine: Boolean,
    modifier: Modifier = Modifier
) {
    val xValueFormatter = remember(points) {
        CartesianValueFormatter { _, value, _ ->
            val index = value.roundToInt().coerceIn(0, points.size - 1)
            points[index].date.toShortDateLabel()
        }
    }
    // Line 1: the full date (with year) -- same shape [PeriodChart]'s own marker uses, see
    // [rememberPeriodChartMarker]'s doc comment. Line 2: valueDisplay plus percent change from the
    // series' own first point to whichever point is touched.
    val marker = rememberPeriodChartMarker(points.size) { index ->
        val point = points[index]
        "${point.date.toMarkerDateLabel()}\n${point.valueDisplay ?: point.value.toString()}  ${percentChangeFrom(points.first().value, point.value)}"
    }
    // Up to 5 labels, always including both endpoints -- see FixedItemPlacer's doc comment
    // (PeriodChart.kt, same package) for why this isn't Vico's own spacing-based aligned() placer.
    val labelIndices = remember(points) { evenlySpacedIndices(points.size, LABEL_COUNT) }
    val itemPlacer = remember(labelIndices) { FixedItemPlacer(labelIndices) }
    val pointConnector = remember(isStepLine) {
        if (isStepLine) StepAfterPointConnector else LineCartesianLayer.PointConnector.cubic()
    }

    VicoLinePlot(
        prices = points.map { it.value },
        lineColor = lineColor,
        xValueFormatter = xValueFormatter,
        itemPlacer = itemPlacer,
        marker = marker,
        modifier = modifier,
        pointConnector = pointConnector
    )
}

/**
 * Draws a right-angle "step-after" line -- flat from the previous point's own value out to this
 * point's x-position, then a vertical jump to this point's value -- rather than a straight or
 * curved segment between the two. Communicates "this is what the metric held at until the next
 * actual release" for the monthly/quarterly macro metrics, where the gap between two real points
 * is often weeks, honestly rather than implying a smooth trend between them. Vico only ships
 * `Sharp` (straight line) and `cubic()` built in, so this is a one-line custom implementation of
 * the same `PointConnector` fun interface `Sharp` itself uses.
 */
private val StepAfterPointConnector = LineCartesianLayer.PointConnector { _, path, _, y1, x2, y2 ->
    path.lineTo(x2, y1)
    path.lineTo(x2, y2)
}

/**
 * A single recorded reading can't draw a line -- [VicoLinePlot] needs at least two points -- so
 * this shows [point]'s own value and date as a centered marker instead of an empty-looking plot
 * area. Reuses the same reserved [R.dimen.stock_detail_chart_reserved_height] box the real chart
 * and the "no history yet" empty state both render into, so this state doesn't shift the layout
 * around it any differently than either of those two do.
 */
@Composable
private fun SingleHistoryPointDisplay(point: MetricHistoryPoint, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = point.valueDisplay ?: point.value.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = stringResource(
                id = R.string.metric_history_single_point_caption,
                point.date.toShortDateLabel()
            ),
            style = MaterialTheme.typography.labelSmall,
            color = LocalPulseColors.current.onSurfaceMuted
        )
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val mockIndicatorPoints = listOf(
    MetricHistoryPoint("2026-08-21", 63.42, "63.42%", SignalColor.YELLOW),
    MetricHistoryPoint("2026-08-22", 64.10, "64.10%", SignalColor.YELLOW),
    MetricHistoryPoint("2026-08-23", 61.05, "61.05%", SignalColor.YELLOW),
    MetricHistoryPoint("2026-08-24", 58.30, "58.30%", SignalColor.RED),
    MetricHistoryPoint("2026-08-25", 55.75, "55.75%", SignalColor.RED)
)

// Sparse, irregularly-spaced points -- same shape a real cpi_yoy series has (a handful of points
// a year, real gaps between them, not one-per-day) -- to preview the step-after line.
private val mockMacroCadencePoints = listOf(
    MetricHistoryPoint("2026-02-12", 3.1, "3.1%", SignalColor.YELLOW),
    MetricHistoryPoint("2026-04-10", 3.3, "3.3%", SignalColor.YELLOW),
    MetricHistoryPoint("2026-06-11", 3.0, "3.0%", SignalColor.GREEN),
    MetricHistoryPoint("2026-08-13", 2.8, "2.8%", SignalColor.GREEN)
)

private val mockSinglePoint = listOf(
    MetricHistoryPoint("2026-08-25", 22.50, "22.50x", SignalColor.YELLOW)
)

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewIndicatorHistoryChartLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        IndicatorHistoryChart(points = mockIndicatorPoints, isStepLine = false)
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewIndicatorHistoryChartDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        IndicatorHistoryChart(points = mockIndicatorPoints, isStepLine = false)
    }
}

@Preview(name = "Macro cadence (step line)", showBackground = true)
@Composable
private fun PreviewIndicatorHistoryChartMacroCadence() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        IndicatorHistoryChart(points = mockMacroCadencePoints, isStepLine = true)
    }
}

@Preview(name = "Single point", showBackground = true)
@Composable
private fun PreviewIndicatorHistoryChartSinglePoint() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        IndicatorHistoryChart(points = mockSinglePoint, isStepLine = false)
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun PreviewIndicatorHistoryChartLoading() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        IndicatorHistoryChart(points = emptyList(), isStepLine = false, isLoading = true)
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun PreviewIndicatorHistoryChartEmpty() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        IndicatorHistoryChart(points = emptyList(), isStepLine = false, isLoading = false)
    }
}
