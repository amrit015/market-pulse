package com.marketlabs.pulse.storage.model.intraday

/**
 * Today's intraday bars for one symbol, from `GET /intraday/:symbol` — in-memory only, never
 * persisted (resets every trading day server-side, so caching it locally would be actively wrong,
 * not just unnecessary). `previousClose` is the sparkline's baseline (yesterday's close) — a
 * different baseline than the period chart's (`ChartSeries` uses the first point in its own
 * range), don't conflate the two.
 */
data class IntradaySeries(
    val symbol: String,
    val date: String,
    val previousClose: Double?,
    val points: List<IntradayPoint>
)

/** One bar. `minutesSinceMidnightEt` comes from parsing the response's `"HH:mm"` time string. */
data class IntradayPoint(
    val minutesSinceMidnightEt: Int,
    val price: Double
)
