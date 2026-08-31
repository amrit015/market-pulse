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
 * No range picker -- the backend spec explicitly says not to build one yet (history only goes
 * back to 2026-08-21), so this always renders whatever the single cached/fetched series holds.
 */
@Composable
fun IndicatorHistoryChart(
    points: List<MetricHistoryPoint>,
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
                points.isNotEmpty() ->
                    IndicatorHistoryChartPlot(points = points, lineColor = lineColor, modifier = Modifier.fillMaxSize())
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
            // the marker below.
            if (points.isNotEmpty()) {
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
    modifier: Modifier = Modifier
) {
    val xValueFormatter = remember(points) {
        CartesianValueFormatter { _, value, _ ->
            val index = value.roundToInt().coerceIn(0, points.size - 1)
            points[index].date.toShortDateLabel()
        }
    }
    // Second line: percent change from the series' own first point to whichever point is touched.
    val marker = rememberPeriodChartMarker(points.size) { index ->
        val point = points[index]
        "${point.date.toShortDateLabel()}, ${point.valueDisplay ?: point.value.toString()}\n${percentChangeFrom(points.first().value, point.value)}"
    }
    // Up to 5 labels, always including both endpoints -- see FixedItemPlacer's doc comment
    // (PeriodChart.kt, same package) for why this isn't Vico's own spacing-based aligned() placer.
    val labelIndices = remember(points) { evenlySpacedIndices(points.size, LABEL_COUNT) }
    val itemPlacer = remember(labelIndices) { FixedItemPlacer(labelIndices) }

    VicoLinePlot(
        prices = points.map { it.value },
        lineColor = lineColor,
        xValueFormatter = xValueFormatter,
        itemPlacer = itemPlacer,
        marker = marker,
        modifier = modifier
    )
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

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewIndicatorHistoryChartLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        IndicatorHistoryChart(points = mockIndicatorPoints)
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewIndicatorHistoryChartDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        IndicatorHistoryChart(points = mockIndicatorPoints)
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun PreviewIndicatorHistoryChartLoading() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        IndicatorHistoryChart(points = emptyList(), isLoading = true)
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun PreviewIndicatorHistoryChartEmpty() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        IndicatorHistoryChart(points = emptyList(), isLoading = false)
    }
}
