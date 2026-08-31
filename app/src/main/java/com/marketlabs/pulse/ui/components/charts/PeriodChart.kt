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
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.LineComponent
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
 *
 * [currentPrice], when supplied, replaces the last point's price for display purposes if that
 * point's own date is today (ET) -- `market_charts`' daily close for the current trading day is
 * only written once the session ends, so while the market's still open the stored point can lag
 * behind the live price this same screen's header already shows (`AssetOverview`/`StockPreview`'s
 * own `price` field), making the chart's own endpoint disagree with the rest of the page. [points]
 * itself is left untouched by the caller either way -- this only affects what gets drawn/captioned.
 *
 * [useAccentColor] skips the bullish/bearish direction read entirely in favor of the app's own
 * accent color -- for dashboard readings that run on their own up/down logic that doesn't map to
 * green-is-good/red-is-bad the way a price does (VIX, Fear & Greed, Put/Call: e.g. a rising VIX is
 * conventionally bearish, not "up is good"), tinting the line green or red the normal way would
 * misstate the reading.
 */
@Composable
fun PeriodChart(
    points: List<ChartPoint>,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    currentPrice: Double? = null,
    useAccentColor: Boolean = false
) {
    val pulseColors = LocalPulseColors.current
    val effectivePoints = if (currentPrice != null) points.withCurrentPriceForToday(currentPrice) else points
    val lineColor = if (effectivePoints.isNotEmpty()) {
        if (useAccentColor) pulseColors.accentPrimary else periodChartLineColor(effectivePoints, pulseColors)
    } else null

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.stock_detail_chart_reserved_height)),
            contentAlignment = Alignment.Center
        ) {
            when {
                effectivePoints.isNotEmpty() && lineColor != null ->
                    PeriodChartPlot(points = effectivePoints, lineColor = lineColor, modifier = Modifier.fillMaxSize())
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
            if (effectivePoints.isNotEmpty() && lineColor != null) {
                Text(
                    text = priceFormat.format(effectivePoints.first().price),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = lineColor
                )
                Text(
                    text = priceFormat.format(effectivePoints.last().price),
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

/**
 * Swaps the last point's price for [currentPrice] when that point's own `date` is today (ET) --
 * see [PeriodChart]'s doc comment for why. Every other point, and today's own point once its
 * recorded price already matches, is returned as-is (no allocation).
 */
private fun List<ChartPoint>.withCurrentPriceForToday(currentPrice: Double): List<ChartPoint> {
    val last = lastOrNull() ?: return this
    if (last.price == currentPrice || last.date != LocalDate.now(easternZoneId).toString()) return this
    return dropLast(1) + last.copy(price = currentPrice)
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
    // Second line: percent change from the range's own first point (same baseline the caption
    // row and periodChartLineColor already use) to whichever point is touched -- not a fixed
    // day-over-day delta, since that's not what this chart's baseline means (see PeriodChart's
    // own doc comment on why the first point in the returned range is the only correct baseline
    // for a multi-day series).
    val marker = rememberPeriodChartMarker(points.size) { index ->
        val point = points[index]
        "${point.date.toShortDateLabel()}, ${priceFormat.format(point.price)}\n${percentChangeFrom(points.first().price, point.price)}"
    }
    // Up to 5 labels spread evenly across the series, always including both endpoints -- see
    // FixedItemPlacer's doc comment for why this isn't Vico's own spacing-based aligned() placer.
    val labelIndices = remember(points) { evenlySpacedIndices(points.size, LABEL_COUNT) }
    val itemPlacer = remember(labelIndices) { FixedItemPlacer(labelIndices) }
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
 *
 * [referenceLineValue], when supplied, draws a flat, faint horizontal line across the full width
 * at that y-value -- [IntradayPeriodChart] uses this for the 1D chart's previous-close baseline,
 * so it's easy to see at a glance whether today's line is running above or below it. Solid rather
 * than dashed (Vico's [LineComponent] has no dash-pattern support), so "faint" comes entirely from
 * a low-alpha version of the chart's own line color, keeping the same bullish/bearish association
 * without visually competing with the real price line. `null` (the default) draws nothing extra --
 * [PeriodChart]'s multi-day ranges and [IndicatorHistoryChart] have no such fixed baseline to show.
 */
@Composable
internal fun VicoLinePlot(
    prices: List<Double>,
    lineColor: Color,
    xValueFormatter: CartesianValueFormatter,
    itemPlacer: HorizontalAxis.ItemPlacer,
    marker: CartesianMarker,
    modifier: Modifier = Modifier,
    referenceLineValue: Double? = null
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
        // across the whole area, not just hug the line at the top. (Was 0.45f/0.1f -- bumped up
        // further for the same reason SparklineChart's own gradient was: too faded to read as an
        // actual color, especially toward the bottom.)
        colors = listOf(lineColor.copy(alpha = 0.6f), lineColor.copy(alpha = 0.25f))
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

    val decorations = remember(referenceLineValue, lineColorInt) {
        val value = referenceLineValue
        if (value == null) {
            emptyList()
        } else {
            listOf(
                HorizontalLine(
                    y = { value },
                    line = LineComponent(fill = Fill(lineColor.copy(alpha = 0.4f).toArgb()), thicknessDp = 1f)
                )
            )
        }
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
                marker = marker,
                decorations = decorations
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
 *
 * [useAccentColor] -- see [PeriodChart]'s doc comment; same non-price-direction reasoning applies
 * to this chart's own 1D line.
 */
@Composable
fun IntradayPeriodChart(
    points: List<IntradayPoint>,
    previousClose: Double?,
    date: String?,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    useAccentColor: Boolean = false
) {
    val pulseColors = LocalPulseColors.current
    // See PeriodChart's doc comment on useAccentColor -- same reasoning applies here for VIX/
    // Fear & Greed/Put-Call's own 1D charts.
    val lineColor = if (points.isNotEmpty()) {
        if (useAccentColor) pulseColors.accentPrimary else intradayChartLineColor(points, previousClose, pulseColors)
    } else null

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.stock_detail_chart_reserved_height)),
            contentAlignment = Alignment.Center
        ) {
            when {
                points.isNotEmpty() && lineColor != null ->
                    IntradayChartPlot(
                        points = points,
                        date = date,
                        previousClose = previousClose,
                        lineColor = lineColor,
                        modifier = Modifier.fillMaxSize()
                    )
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
 * Builds [IntradayPeriodChart]'s local-time x-axis/marker text and delegates to [VicoLinePlot].
 *
 * Labels are evenly spaced by index (up to 5 across the series, always including both endpoints --
 * see [evenlySpacedIndices]/[FixedItemPlacer]), not placed at real clock-hour boundaries -- an
 * earlier version labeled every hour, which reads fine for a 6.5-hour equity session but crowds
 * into an unreadable wall of labels for a 24-hour one (crypto's `12 AM`...`11 PM` span). Since
 * labels don't necessarily land exactly on the hour, both the axis and the marker show minute
 * precision.
 */
@Composable
private fun IntradayChartPlot(
    points: List<IntradayPoint>,
    date: String?,
    previousClose: Double?,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val localTimes = remember(points, date) { points.toLocalTimes(date) }

    val xValueFormatter = remember(localTimes) {
        CartesianValueFormatter { _, value, _ ->
            val index = value.roundToInt().coerceIn(0, localTimes.size - 1)
            markerTimeFormatter.format(localTimes[index])
        }
    }
    // First line: calendar date + local time -- the x-axis below only has room for the time, so
    // the marker is the only place this chart states which day "today" actually was (relevant the
    // moment a viewer is scrubbing a session in a different time zone than the market's, where the
    // local clock time alone can appear to belong to a different calendar day). Second line:
    // formatted price + percent change from previousClose, not today's own first tick -- unlike
    // every other range (where the first point in the shown series is the only baseline that
    // exists), 1D's line color/direction is already anchored to previousClose (see
    // intradayChartLineColor), and computing the marker's percent against a different baseline
    // (the open) could disagree with that color -- e.g. read green while showing a negative
    // percent, if today opened above previousClose but the touched point sits below the open.
    // Falls back to the open only if previousClose is unavailable, same fallback
    // intradayChartLineColor itself uses.
    val marker = rememberPeriodChartMarker(points.size) { index ->
        val point = points[index]
        val baseline = previousClose ?: points.first().price
        val localTime = localTimes[index]
        "${markerDateFormatter.format(localTime)}, ${markerTimeFormatter.format(localTime)}\n" +
            "${priceFormat.format(point.price)}  ${percentChangeFrom(baseline, point.price)}"
    }
    val labelIndices = remember(points) { evenlySpacedIndices(points.size, LABEL_COUNT) }
    val itemPlacer = remember(labelIndices) { FixedItemPlacer(labelIndices) }

    VicoLinePlot(
        prices = points.map { it.price },
        lineColor = lineColor,
        xValueFormatter = xValueFormatter,
        itemPlacer = itemPlacer,
        marker = marker,
        modifier = modifier,
        // Faint flat previous-close baseline -- see VicoLinePlot's doc comment on
        // referenceLineValue. `null` (no line drawn) only when there's genuinely no previous
        // close to compare against.
        referenceLineValue = previousClose
    )
}

/** How many x-axis labels [PeriodChartPlot]/[IntradayChartPlot]/`IndicatorHistoryChartPlot` each aim for. */
internal const val LABEL_COUNT = 5

/**
 * Picks up to [count] indices evenly spread across `[0, size)`, always including both endpoints
 * (`0` and `size - 1`) once `size >= 2`. Deliberately not Vico's own spacing-based `aligned()`
 * placer: that one lays labels out as an arithmetic progression from the series' first index (`0`,
 * `spacing`, `2*spacing`, ...), which has no reason to ever land on the *last* index -- in practice
 * the chart's newest point often ended up with no x-axis label at all, or whatever became the
 * rightmost label got squeezed into an unreadable "…" right at the chart's true edge (its reserved
 * end-padding is computed from a modular-arithmetic value that doesn't necessarily match any label
 * actually being drawn). Reserving the endpoints explicitly, with real measured padding for them
 * (see [FixedItemPlacer.getFirstLabelValue]/[getLastLabelValue]), fixes both. `internal`, not
 * `private` -- `IndicatorHistoryChart` (separate file, same package) uses this too.
 */
internal fun evenlySpacedIndices(size: Int, count: Int): List<Double> {
    if (size <= 0) return emptyList()
    if (size <= count) return (0 until size).map { it.toDouble() }
    val step = (size - 1).toDouble() / (count - 1)
    return (0 until count).map { i -> (i * step).roundToInt().toDouble() }.distinct()
}

/**
 * A [HorizontalAxis.ItemPlacer] that labels exactly the indices in [labelValues] -- see
 * [evenlySpacedIndices]'s doc comment for why this replaces Vico's own `aligned()` placer.
 * `getFirstLabelValue`/`getLastLabelValue` (both `null` by default, meaning "no reserved padding")
 * are overridden so the axis actually measures and reserves real space for the first/last labels,
 * the same way `AlignedHorizontalAxisItemPlacer`'s own `addExtremeLabelPadding` does -- without
 * this, the endpoint labels are positioned correctly but can still get clipped since half their
 * text width would otherwise extend past the chart's plotted edge into unreserved space.
 * `getWidthMeasurementLabelValues`/`getHeightMeasurementLabelValues` fall back to a single dummy
 * value when [labelValues] is empty: Vico's own `HorizontalAxis` calls `.maxOf` (not `maxOfOrNull`)
 * on the height list, so an empty list would crash rather than just render no labels.
 */
internal class FixedItemPlacer(
    private val labelValues: List<Double>
) : HorizontalAxis.ItemPlacer {
    private val measurementValues = labelValues.ifEmpty { listOf(0.0) }

    override fun getFirstLabelValue(context: CartesianMeasuringContext, maxLabelWidth: Float): Double? =
        labelValues.firstOrNull()

    override fun getLastLabelValue(context: CartesianMeasuringContext, maxLabelWidth: Float): Double? =
        labelValues.lastOrNull()

    override fun getLabelValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float
    ): List<Double> = labelValues.filter { it in visibleXRange }

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

    // Same formula AlignedHorizontalAxisItemPlacer uses: the tick's own space, minus whatever
    // padding getFirstLabelValue/getLastLabelValue already reserved for the endpoint label's text,
    // so the two don't double up.
    override fun getStartLayerMargin(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        tickThickness: Float,
        maxLabelWidth: Float
    ): Float = (tickThickness - layerDimensions.unscalableStartPadding).coerceAtLeast(0f)

    override fun getEndLayerMargin(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        tickThickness: Float,
        maxLabelWidth: Float
    ): Float = (tickThickness - layerDimensions.unscalableEndPadding).coerceAtLeast(0f)
}

/**
 * `"+5.23%"` / `"-3.10%"` -- signed percent change from [baseline] to [value], the marker's second
 * line across all three chart types. `internal`, not `private` -- `IndicatorHistoryChart`
 * (separate file, same package) uses this too. `"--"` on a zero baseline rather than dividing by
 * it -- a metric reading of exactly 0 is a real possibility (e.g. a rate at 0%), not just a
 * defensive edge case.
 */
internal fun percentChangeFrom(baseline: Double, value: Double): String {
    if (baseline == 0.0) return "--"
    val percent = (value - baseline) / baseline * 100
    val sign = if (percent >= 0) "+" else ""
    return "$sign${"%.2f".format(percent)}%"
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

/** Axis and marker labels, minute-precise: `"12:01 AM"`, `"9:34 AM"`. */
private val markerTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

/** 1D marker's date line, in the viewer's local calendar date: `"Aug 24"`. */
private val markerDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)

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
