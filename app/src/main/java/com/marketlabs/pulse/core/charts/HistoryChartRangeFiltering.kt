package com.marketlabs.pulse.core.charts

import com.marketlabs.pulse.storage.model.charts.ChartRange
import java.time.LocalDate

/**
 * Shared range-picker filtering for a metric-history-style chart (Indicators' `MetricDetailViewModel`,
 * Posture/Positioning's `GlossaryDetailViewModel`) -- both domains fetch one flat, already-capped
 * series per metric with no backend range parameter, so switching ranges is always a client-side
 * re-filter of that one cached series, never a new fetch. Generic over the point type ([T]) rather
 * than tied to either domain's own point shape (`MetricHistoryPoint` has a backend `valueDisplay`/
 * `signalColor`, `InsightsHistoryPoint` doesn't) -- [dateOf] is the one thing this logic actually
 * needs from a point. `internal`, not tied to either ViewModel's own package, since both need the
 * exact same logic and it would otherwise have been duplicated (or type-coerced) between them.
 */

/**
 * Filters the already-fetched series down to points whose own date ([dateOf]) actually falls within
 * [range]'s real calendar window as of [today] -- a genuine date cutoff, not [ChartRange.days]'s
 * trading-day *point count* (that field means something different for `market_charts`' one point
 * per trading day than it does here, where a monthly metric can have zero points in the last 30
 * calendar days and `takeLast(21)` would silently return the whole series instead). [ChartRange.YTD]
 * has no `days` value at all -- filtered against January 1st of [today]'s year instead.
 * [ChartRange.ONE_DAY] never reaches here in practice, excluded from `computeAvailableChartRanges`
 * since neither domain's history endpoint has intraday granularity for any metric.
 */
internal fun <T> List<T>.filteredForRange(range: ChartRange, dateOf: (T) -> String, today: LocalDate = LocalDate.now()): List<T> {
    val cutoff = when (range) {
        ChartRange.FIVE_DAY -> today.minusDays(5)
        ChartRange.ONE_MONTH -> today.minusMonths(1)
        ChartRange.SIX_MONTH -> today.minusMonths(6)
        ChartRange.YTD -> LocalDate.of(today.year, 1, 1)
        ChartRange.ONE_YEAR -> today.minusYears(1)
        ChartRange.ONE_DAY -> return this
    }
    return filter { point -> dateOf(point).toLocalDateOrNull()?.let { !it.isBefore(cutoff) } ?: false }
}

/**
 * Which range buttons are worth showing for this specific metric's actual data, computed from real
 * point dates rather than [isCoveredByHistory][com.marketlabs.pulse.storage.model.charts.isCoveredByHistory]'s
 * "does history reach back this far" check -- that one only asks whether history goes back far
 * enough, which is the wrong question for a series that isn't one point per calendar day (a macro
 * metric with 12 points spread one-a-month across a year *does* reach back further than 30 days, so
 * that check alone would still offer a "1M" button that renders identically to "6M"/"1Y" since a
 * true 1-month window around any single point only ever contains that one point).
 *
 * Walks the candidate ranges narrowest-to-widest (`ChartRange`'s own declaration order, minus
 * `ONE_DAY` -- neither domain's history endpoint has intraday granularity) and keeps a range only
 * when [filteredForRange] on it returns:
 * - at least 2 points -- a single-point (or empty) slice isn't a meaningfully different *range* to
 *   pick, even though [IndicatorHistoryChart][com.marketlabs.pulse.ui.components.charts.IndicatorHistoryChart]
 *   itself renders a lone point gracefully when that's genuinely all the series holds;
 * - a different point count than the narrowest range already kept -- a wider range that returns the
 *   exact same points as a narrower one already offered is a redundant button showing identical data
 *   (the 12-points-over-12-months case: "1M" and "5D" both round down to 0-1 points and get dropped,
 *   "6M"/"YTD"/"1Y" each genuinely add more of the year's real releases and all three survive).
 *
 * A metric whose data can't clear that bar for more than one range (e.g. a single recorded point,
 * or every point crammed into the last few days) returns a list of 0 or 1 -- callers hide the picker
 * entirely in that case rather than showing a control with nothing to pick between (see
 * [resolveEffectiveRange]).
 */
internal fun <T> List<T>.computeAvailableChartRanges(dateOf: (T) -> String, today: LocalDate = LocalDate.now()): List<ChartRange> {
    var lastKeptCount = 0
    return ChartRange.entries.filter { it != ChartRange.ONE_DAY }.filter { range ->
        val count = filteredForRange(range, dateOf, today).size
        (count >= 2 && count != lastKeptCount).also { kept -> if (kept) lastKeptCount = count }
    }
}

/**
 * Resolves what should actually be applied/shown given [available] -- `null` when [available] has 0
 * or 1 entries (a picker with nothing real to pick between; the caller shows the full unfiltered
 * series and hides the range picker), [selected] itself when it's still a valid choice, or the
 * widest available range as a graceful fallback when [selected] was pruned out by
 * [computeAvailableChartRanges] (e.g. a preferred "1M" default that turned out to show fewer than 2
 * points for this specific metric's actual data).
 */
internal fun resolveEffectiveRange(selected: ChartRange, available: List<ChartRange>): ChartRange? = when {
    available.size <= 1 -> null
    selected in available -> selected
    else -> available.last()
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()
