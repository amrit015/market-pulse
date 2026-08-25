package com.marketlabs.pulse.storage.model.charts

import java.time.LocalDate

/**
 * The clean, UI-ready form of a `GET /charts/:symbol` response — one series for one
 * `(symbol, range)` pair. Cached per-range (see `ChartEntity`) rather than as one full series
 * sliced client-side: `?days=30` returns roughly 21 trading days, not literally the last 30
 * stored points, so `days` isn't a simple point-count trim that's safely reproducible outside the
 * backend — only the backend's own filtering per range is trustworthy.
 */
data class ChartSeries(
    val symbol: String,
    val range: ChartRange,
    val name: String? = null,
    val type: String? = null,
    val points: List<ChartPoint> = emptyList(),
    val lastSyncedTimestamp: Long
)

data class ChartPoint(
    val date: String,
    val price: Double
)

/**
 * The period-chart range picker's options. Five of these (5D/1M/6M/YTD/1Y) match exactly what the
 * backend's `/charts/:symbol` supports, using `days`/`queryRange` as that endpoint's own query
 * params; no 5Y/MAX since both would exceed the backend's stored retention cap of ~2 trading years
 * for stocks, less for dashboard assets.
 *
 * `ONE_DAY` is the exception: both its fields are `null` because it's never sent to
 * `/charts/:symbol` at all -- the ViewModel routes it to `IntradayRepository`'s already-polling
 * stream instead (the same bars the sparkline draws from), since that's real intraday data and
 * `market_charts` only ever stores daily closes. Treat a `null` `days`/`queryRange` pair as "this
 * range isn't backend-chart-fetchable," not as "no query params."
 */
enum class ChartRange(val days: Int?, val queryRange: String?, val rangeKey: String) {
    ONE_DAY(days = null, queryRange = null, rangeKey = "1D"),
    FIVE_DAY(days = 5, queryRange = null, rangeKey = "5D"),
    ONE_MONTH(days = 30, queryRange = null, rangeKey = "1M"),
    SIX_MONTH(days = 180, queryRange = null, rangeKey = "6M"),
    YTD(days = null, queryRange = "ytd", rangeKey = "YTD"),
    ONE_YEAR(days = 365, queryRange = null, rangeKey = "1Y")
}

/**
 * Whether a symbol's actual history reaches back far enough for this range to show something
 * distinct from a shorter one -- a 4-month-old listing showing identical (fully-clipped) data
 * under 6M, YTD, *and* 1Y is just three redundant buttons for the same chart. [earliestAvailableDate]
 * is the oldest point date across the widest range the app has actually fetched
 * (`ChartRange.ONE_YEAR`, see `StockDetailViewModel`/`AssetDetailViewModel`'s eager background
 * fetch); `null` means that fetch hasn't resolved yet, and every range is kept optimistically
 * until it does, rather than hiding buttons based on no information.
 *
 * `ONE_DAY` always passes -- it's gated by its own separate intraday-eligibility check, not
 * history depth (a brand-new listing has just as much "today" as a decades-old one).
 * `FIVE_DAY` always passes too -- any history at all is enough for it to be meaningful.
 */
fun ChartRange.isCoveredByHistory(earliestAvailableDate: LocalDate?, today: LocalDate = LocalDate.now()): Boolean {
    if (earliestAvailableDate == null) return true
    return when (this) {
        ChartRange.ONE_DAY, ChartRange.FIVE_DAY -> true
        ChartRange.ONE_MONTH -> !earliestAvailableDate.isAfter(today.minusDays(30))
        ChartRange.SIX_MONTH -> !earliestAvailableDate.isAfter(today.minusDays(180))
        ChartRange.YTD -> !earliestAvailableDate.isAfter(LocalDate.of(today.year, 1, 1))
        ChartRange.ONE_YEAR -> !earliestAvailableDate.isAfter(today.minusDays(365))
    }
}
