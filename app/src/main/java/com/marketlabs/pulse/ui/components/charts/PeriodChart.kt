package com.marketlabs.pulse.ui.components.charts

// Rescoped from the orphaned ui/charts/types/AreaLineChart.kt + RememberMarker.kt (mock-data Vico
// scaffold, never wired into any screen) into the real period-chart component.

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.charts.ChartPoint
import com.marketlabs.pulse.storage.model.intraday.IntradayPoint
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.PulseColors
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A period chart (5D/1M/6M/YTD/1Y) — gradient area-fill, smoothed (cubic) line, no point dots,
 * financial-look line. Colored bullish/bearish off the *range's own* first-vs-last point, not a
 * fixed prior-close -- `market_charts` only ever stores daily closes, no true market "open," so
 * the first close in the returned range is the only available and correct baseline for a
 * multi-day view. Uses `LocalPulseColors`'s signal tokens, not the hardcoded red the original mock
 * scaffold used.
 *
 * Owns all three states -- real data, [isLoading], and empty -- at one fixed total height (plot
 * area + caption row), rather than callers swapping this composable out for their own
 * differently-sized empty-state block. Switching the range picker used to hide the whole chart
 * and show a shorter "No chart history yet" block while the new range's data loaded, which visibly
 * shifted every section below it up and back down on every tap; showing a progress indicator in
 * the same reserved space instead keeps the layout stable through a range switch.
 *
 * Pinch-zoom is disabled and the zoom level is pinned to fit the whole series in view -- with only
 * 5-252 points and no need to inspect sub-ranges, letting someone zoom in/out just hides data
 * behind a gesture for no benefit. Horizontal scroll stays technically enabled (see the
 * `scrollState` comment below) even though there's nothing to actually pan into at a zoom level
 * that already fits everything -- press-and-drag still drives [rememberPeriodChartMarker]'s
 * value+date balloon, scrubbing across points as you drag. The Y-axis range is fixed to the
 * series' own min/max (with headroom) rather than Vico's default,
 * which pins 0 as a hard floor/ceiling whenever every value is positive -- for a price series
 * that's almost always the case, so the default would flatten every chart into a near-straight
 * line regardless of how much the price actually moved. The Y-axis always shows exactly 4 labels
 * (`VerticalAxis.ItemPlacer.count`) rather than Vico's default step-based placer, which can
 * collapse to a single label on a narrow-range series. A caption row below the chart states the
 * first/last price explicitly, colored by the range's own direction -- the bottom axis's own date
 * labels are spaced for readability across the series, not guaranteed to land exactly on the
 * first/last point, so the caption is the reliable way to read the range's start/end price; it
 * doesn't repeat the date, which the x-axis right below it already shows.
 */
@Composable
fun PeriodChart(
    points: List<ChartPoint>,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val pulseColors = LocalPulseColors.current
    val lineColor = if (points.isNotEmpty()) periodChartLineColor(points, pulseColors) else null

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.stock_detail_chart_reserved_height)),
            contentAlignment = Alignment.Center
        ) {
            when {
                points.isNotEmpty() && lineColor != null ->
                    PeriodChartPlot(points = points, lineColor = lineColor, modifier = Modifier.fillMaxSize())
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
            // Prices only -- the dates are already on the x-axis directly below this row, so
            // repeating them here was redundant. Both colored by the period's own direction
            // (the same bullish/bearish read the line and fill already use), not a muted neutral
            // tone -- this row is the "open vs. close for this range" summary, so it should carry
            // the same green/red read as the chart itself. Left empty (but still reserving this
            // row's height) while loading/empty, so that state doesn't change this row's height.
            if (points.isNotEmpty() && lineColor != null) {
                Text(
                    text = priceFormat.format(points.first().price),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = lineColor
                )
                Text(
                    text = priceFormat.format(points.last().price),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = lineColor
                )
            }
        }
    }
}

private fun periodChartLineColor(points: List<ChartPoint>, pulseColors: PulseColors): Color {
    val isBullish = points.last().price >= points.first().price
    return if (isBullish) pulseColors.signalBullishText else pulseColors.signalBearishText
}

/** Builds [PeriodChart]'s date-labeled x-axis/marker text and delegates the actual Vico host to [VicoLinePlot]. */
@Composable
private fun PeriodChartPlot(
    points: List<ChartPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    // Vico probes this for label-width measurement and boundary gridlines too, not just the
    // exact indices real points sit at -- an out-of-range index isn't "no point here," so this
    // clamps into range rather than falling back to an empty string, which Vico treats as a
    // configuration error and crashes on (`CartesianValueFormatter.format returned an empty
    // string`).
    val xValueFormatter = remember(points) {
        CartesianValueFormatter { _, value, _ ->
            val index = value.roundToInt().coerceIn(0, points.size - 1)
            points[index].date.toShortDateLabel()
        }
    }
    val marker = rememberPeriodChartMarker(points.size) { index ->
        "${points[index].date.toShortDateLabel()}  ${priceFormat.format(points[index].price)}"
    }
    // Roughly 5 labels spread evenly across the series, regardless of how many points it holds
    // (5 for a 5D range, ~252 for 1Y) -- a label per point would be unreadable on the longer
    // ranges. [IntradayPeriodChart] uses a different placer (real clock-hour boundaries, not
    // index spacing) since evenly-spaced indices don't line up with the same wall-clock time
    // when a bar is missing -- see [HourBoundaryItemPlacer].
    val xAxisSpacing = remember(points) { (points.size / 5).coerceAtLeast(1) }
    val itemPlacer = remember(xAxisSpacing) { HorizontalAxis.ItemPlacer.aligned(spacing = { xAxisSpacing }) }
    VicoLinePlot(
        prices = points.map { it.price },
        lineColor = lineColor,
        xValueFormatter = xValueFormatter,
        itemPlacer = itemPlacer,
        marker = marker,
        modifier = modifier
    )
}

/**
 * The Vico chart host shared by [PeriodChart] (daily closes), [IntradayPeriodChart] (today's
 * intraday bars), and [IndicatorHistoryChart][com.marketlabs.pulse.ui.components.charts.IndicatorHistoryChart]
 * (indicator metric readings) -- rendering a smoothed, gradient-filled price line is identical
 * between them; only the x-axis/marker label *text* differs (a short date, a time-of-day, or an
 * indicator's own pre-formatted display string), which each caller builds for its own point type
 * and hands in here already-formatted, so this stays agnostic to which domain type it's actually
 * plotting. `internal`, not `private` -- `IndicatorHistoryChart` lives in a separate file (same
 * package) and needs to call this directly rather than duplicating the whole Vico setup.
 */
@Composable
internal fun VicoLinePlot(
    prices: List<Double>,
    lineColor: Color,
    xValueFormatter: CartesianValueFormatter,
    itemPlacer: HorizontalAxis.ItemPlacer,
    marker: CartesianMarker,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(prices) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = prices.indices.map { it.toFloat() },
                    y = prices.map { it.toFloat() }
                )
            }
        }
    }

    val lineColorInt = lineColor.toArgb()
    val gradientBrush = Brush.verticalGradient(
        // Stronger, less-faded wash than a typical sparkline's gradient -- this is the chart's
        // only bullish/bearish cue besides the line itself, so the tint needs to read clearly
        // across the whole area, not just hug the line at the top.
        colors = listOf(lineColor.copy(alpha = 0.45f), lineColor.copy(alpha = 0.1f))
    )

    val lineSpec = remember(lineColorInt, gradientBrush) {
        LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(lineColorInt)),
            stroke = LineCartesianLayer.LineStroke.Continuous(thicknessDp = 2f),
            areaFill = LineCartesianLayer.AreaFill.single(
                fill = Fill(
                    ShaderProvider { _, left, top, right, bottom ->
                        (gradientBrush as ShaderBrush).createShader(Size(right - left, bottom - top))
                    }
                )
            ),
            pointProvider = null,
            pointConnector = LineCartesianLayer.PointConnector.cubic()
        )
    }

    // Headroom above/below the series' own range, not Vico's default 0-anchored range -- see
    // PeriodChart's doc comment for why the default would flatten every chart.
    val rangeProvider = remember(prices) {
        val minPrice = prices.min()
        val maxPrice = prices.max()
        val padding = (maxPrice - minPrice).let { if (it > 0.0) it * 0.1 else maxPrice * 0.05 }
        CartesianLayerRangeProvider.fixed(minY = minPrice - padding, maxY = maxPrice + padding)
    }

    // Whole-number labels ("$550", "$1,240") read fine for stock/index prices, but they flatten
    // every label to the same "0" or "1" for a series that lives under 10 -- the Put/Call ratio
    // (roughly 0.5-1.5) is the concrete case that surfaced this, but any low-priced series would
    // hit the same collapse. Two decimal places once the series' own scale calls for it, matching
    // `priceFormat`'s own precision.
    val yValueFormatter = remember(prices) {
        val maxAbsPrice = prices.maxOf { abs(it) }
        val pattern = if (maxAbsPrice < 10) "#,##0.00" else "#,##0"
        CartesianValueFormatter.decimal(DecimalFormat(pattern))
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(lineSpec),
                    rangeProvider = rangeProvider
                ),
                startAxis = VerticalAxis.rememberStart(
                    line = null,
                    tick = null,
                    valueFormatter = yValueFormatter,
                    itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { 4 }) }
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = xValueFormatter,
                    itemPlacer = itemPlacer
                ),
                marker = marker
                // Marker controller left at its default (showOnPress -- press-and-drag to scrub
                // across points). An earlier attempt disabled Vico's own horizontal-scroll gesture
                // entirely to "fit all data, no scrolling," which left the marker's touch-move
                // tracking as the only gesture handler active over the chart -- and unlike a
                // proper `scrollable`/`draggable` modifier, that raw tracking doesn't participate
                // in Compose's normal orthogonal-direction arbitration against the page's own
                // vertical scroll, so it just won outright regardless of drag direction, making
                // the whole page unscrollable when a drag started over the chart. Re-enabling
                // `scrollState` below (while pinning zoom to Zoom.Content, so there's nothing to
                // actually pan into) keeps Vico's own `scrollable` modifier in the gesture tree,
                // which *does* correctly hand vertical-predominant drags to the page and only
                // claims horizontal-predominant ones -- restoring drag-to-scrub without
                // reintroducing the scroll-blocking bug.
            ),
            modelProducer = modelProducer,
            scrollState = rememberVicoScrollState(scrollEnabled = true),
            zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
            modifier = modifier
        )
    }
}

/**
 * The 1D period chart -- plots [IntradayRepository][com.marketlabs.pulse.core.intraday.IntradayRepository]'s
 * live-polled bars for today (the same data [SparklineChart][com.marketlabs.pulse.ui.components.widgets.SparklineChart]
 * draws from) through the heavier, interactive Vico host instead of the sparkline's cheap Canvas
 * one. Deliberately a sibling composable to [PeriodChart], not a second data-shape branch inside
 * it: the two disagree on both axis semantics (time-of-day vs. calendar date) and bullish/bearish
 * baseline (yesterday's close vs. the range's own first point -- `market_charts`' daily closes
 * have no meaningful "prior close" reference of their own the way a `previousClose` field does),
 * so folding them into one signature would mean two mutually-exclusive parameter groups. They
 * share [VicoLinePlot] for the actual chart host, which is where the code that's genuinely
 * identical between them lives.
 *
 * [date] is the series' own ET trading-day date (`IntradaySeries.date`) -- bars only carry
 * minutes-since-midnight-ET, so this is what pins down an actual instant for each one, which in
 * turn is what lets the x-axis and marker labels convert to and display the *viewer's* local
 * clock time rather than a raw ET reading.
 */
@Composable
fun IntradayPeriodChart(
    points: List<IntradayPoint>,
    previousClose: Double?,
    date: String?,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val pulseColors = LocalPulseColors.current
    val lineColor = if (points.isNotEmpty()) intradayChartLineColor(points, previousClose, pulseColors) else null

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.stock_detail_chart_reserved_height)),
            contentAlignment = Alignment.Center
        ) {
            when {
                points.isNotEmpty() && lineColor != null ->
                    IntradayChartPlot(points = points, date = date, lineColor = lineColor, modifier = Modifier.fillMaxSize())
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
            // Left side is today's own first tick (the 1D "open"), not previousClose -- the
            // header above this chart already has its own "Previous Close" field for yesterday's
            // value, so repeating it here would be redundant. This still matches every other
            // range's caption convention (first vs. last point in what's shown); only the line's
            // bullish/bearish coloring stays anchored to previousClose, since "up/down for the
            // day" is conventionally read against yesterday's close, not today's open.
            if (points.isNotEmpty() && lineColor != null) {
                Text(
                    text = priceFormat.format(points.first().price),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = lineColor
                )
                Text(
                    text = priceFormat.format(points.last().price),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = lineColor
                )
            }
        }
    }
}

private fun intradayChartLineColor(points: List<IntradayPoint>, previousClose: Double?, pulseColors: PulseColors): Color {
    val isBullish = previousClose == null || points.last().price >= previousClose
    return if (isBullish) pulseColors.signalBullishText else pulseColors.signalBearishText
}

/**
 * Builds [IntradayPeriodChart]'s local-time x-axis/marker text and a clock-hour-aligned axis
 * placer, then delegates to [VicoLinePlot].
 */
@Composable
private fun IntradayChartPlot(
    points: List<IntradayPoint>,
    date: String?,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val localTimes = remember(points, date) { points.toLocalTimes(date) }

    // Real clock-hour boundaries (12 AM, 1 AM, ...) computed from each bar's actual converted
    // timestamp, not an evenly-spaced index formula -- bars aren't guaranteed perfectly
    // contiguous (a missed poll tick, or the commodities session's daily maintenance gap), so an
    // index-spacing placer would silently drift out of alignment with real clock hours the moment
    // a bar is missing.
    val hourBoundaryValues = remember(localTimes) {
        localTimes.indices.filter { localTimes[it].minute == 0 }.map { it.toDouble() }
    }
    val itemPlacer = remember(hourBoundaryValues) { HourBoundaryItemPlacer(hourBoundaryValues) }

    val xValueFormatter = remember(localTimes) {
        CartesianValueFormatter { _, value, _ ->
            val index = value.roundToInt().coerceIn(0, localTimes.size - 1)
            hourAxisTimeFormatter.format(localTimes[index])
        }
    }
    val marker = rememberPeriodChartMarker(points.size) { index ->
        "${markerTimeFormatter.format(localTimes[index])}  ${priceFormat.format(points[index].price)}"
    }
    VicoLinePlot(
        prices = points.map { it.price },
        lineColor = lineColor,
        xValueFormatter = xValueFormatter,
        itemPlacer = itemPlacer,
        marker = marker,
        modifier = modifier
    )
}

/**
 * A [HorizontalAxis.ItemPlacer] that only labels the indices in [hourBoundaryValues] -- see
 * [IntradayChartPlot]'s comment on why this is computed from real timestamps rather than a fixed
 * spacing formula. `getWidthMeasurementLabelValues`/`getHeightMeasurementLabelValues` fall back to
 * a single dummy value when there are no hour boundaries in view (e.g. the first few minutes of a
 * session): Vico's own [HorizontalAxis] calls `.maxOf` (not `maxOfOrNull`) on the height list, so
 * an empty list would crash rather than just render no labels.
 */
private class HourBoundaryItemPlacer(
    private val hourBoundaryValues: List<Double>
) : HorizontalAxis.ItemPlacer {
    private val measurementValues = hourBoundaryValues.ifEmpty { listOf(0.0) }

    override fun getLabelValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float
    ): List<Double> = hourBoundaryValues.filter { it in visibleXRange }

    override fun getWidthMeasurementLabelValues(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        fullXRange: ClosedFloatingPointRange<Double>
    ): List<Double> = measurementValues

    override fun getHeightMeasurementLabelValues(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float
    ): List<Double> = measurementValues

    override fun getStartLayerMargin(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        tickThickness: Float,
        maxLabelWidth: Float
    ): Float = tickThickness

    override fun getEndLayerMargin(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        tickThickness: Float,
        maxLabelWidth: Float
    ): Float = tickThickness
}

/** Shared with `PeriodChartMarker.kt` (same package) so the caption and the marker balloon format identically. */
internal val priceFormat = DecimalFormat("$#,##0.00")
private val shortDateFormatter = DateTimeFormatter.ofPattern("MMM d")

/** `"2026-08-01"` -> `"Aug 1"`; falls back to the raw string if it isn't a plain ISO date. */
internal fun String.toShortDateLabel(): String = try {
    LocalDate.parse(this).format(shortDateFormatter)
} catch (e: DateTimeParseException) {
    this
}

private val easternZoneId = ZoneId.of("America/New_York")

/** Hour-boundary axis labels: `"12 AM"`, `"1 AM"`, ... `"11 PM"`. No minutes -- only ever used at :00. */
private val hourAxisTimeFormatter = DateTimeFormatter.ofPattern("h a", Locale.US)

/** Marker/touch labels, minute-precise: `"12:01 AM"`, `"9:34 AM"`. */
private val markerTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

/**
 * Converts each bar's ET-relative `minutesSinceMidnightEt` into the *viewer's* local time --
 * pinning the trading day's own ET calendar date first (bars don't carry a date of their own),
 * then shifting to [ZoneId.systemDefault]. A raw minutes-since-midnight value can't just be
 * relabeled in a different zone's clock hours directly: the offset from ET isn't a whole number
 * of hours for every device timezone (e.g. India is UTC+5:30), so this always resolves a real
 * instant first and lets `java.time` do the actual conversion.
 */
private fun List<IntradayPoint>.toLocalTimes(etDate: String?): List<ZonedDateTime> {
    val startOfDayEt = (etDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now(easternZoneId))
        .atStartOfDay(easternZoneId)
    return map { point ->
        startOfDayEt.plusMinutes(point.minutesSinceMidnightEt.toLong()).withZoneSameInstant(ZoneId.systemDefault())
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val mockBullishPoints = listOf(
    ChartPoint("2026-08-01", 220.0), ChartPoint("2026-08-04", 224.5), ChartPoint("2026-08-05", 222.0),
    ChartPoint("2026-08-06", 228.0), ChartPoint("2026-08-07", 231.5), ChartPoint("2026-08-08", 229.0),
    ChartPoint("2026-08-11", 236.0)
)

private val mockBearishPoints = listOf(
    ChartPoint("2026-08-01", 236.0), ChartPoint("2026-08-04", 231.5), ChartPoint("2026-08-05", 233.0),
    ChartPoint("2026-08-06", 228.0), ChartPoint("2026-08-07", 224.0), ChartPoint("2026-08-08", 226.5),
    ChartPoint("2026-08-11", 220.0)
)

@Preview(name = "Bullish - Light", showBackground = true)
@Composable
private fun PreviewPeriodChartBullishLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PeriodChart(points = mockBullishPoints)
    }
}

@Preview(name = "Bearish - Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewPeriodChartBearishDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PeriodChart(points = mockBearishPoints)
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun PreviewPeriodChartLoading() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PeriodChart(points = emptyList(), isLoading = true)
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun PreviewPeriodChartEmpty() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PeriodChart(points = emptyList(), isLoading = false)
    }
}

private val mockIntradayRisingPoints = listOf(
    IntradayPoint(570, 220.0), IntradayPoint(600, 221.5), IntradayPoint(630, 219.8),
    IntradayPoint(660, 223.0), IntradayPoint(690, 225.4), IntradayPoint(720, 224.1),
    IntradayPoint(750, 227.0)
)

private val mockIntradayFallingPoints = listOf(
    IntradayPoint(570, 227.0), IntradayPoint(600, 225.4), IntradayPoint(630, 226.1),
    IntradayPoint(660, 223.0), IntradayPoint(690, 220.8), IntradayPoint(720, 221.5),
    IntradayPoint(750, 219.0)
)

@Preview(name = "Intraday Bullish - Light", showBackground = true)
@Composable
private fun PreviewIntradayPeriodChartBullishLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        IntradayPeriodChart(points = mockIntradayRisingPoints, previousClose = 220.5, date = "2026-08-24")
    }
}

@Preview(name = "Intraday Bearish - Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewIntradayPeriodChartBearishDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        IntradayPeriodChart(points = mockIntradayFallingPoints, previousClose = 226.0, date = "2026-08-24")
    }
}

@Preview(name = "Intraday Loading", showBackground = true)
@Composable
private fun PreviewIntradayPeriodChartLoading() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        IntradayPeriodChart(points = emptyList(), previousClose = null, date = null, isLoading = true)
    }
}

@Preview(name = "Intraday Empty", showBackground = true)
@Composable
private fun PreviewIntradayPeriodChartEmpty() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        IntradayPeriodChart(points = emptyList(), previousClose = null, date = null, isLoading = false)
    }
}
