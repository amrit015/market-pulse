package com.marketlabs.pulse.network.store.charts

import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.model.charts.ChartSeries

interface RemoteChartDataSource {

    /**
     * Fetches one symbol's daily-close series for `range` from `GET /charts/:symbol`. A `404`
     * (symbol has no chart doc yet — e.g. added to tracking today, before its first EOD run) is
     * "no data yet," not a failure, so it resolves to `Result.success(null)` rather than
     * `Result.failure` — callers should treat a `null` success the same as an empty state, and
     * reserve `Result.failure` for genuine network/server errors.
     */
    suspend fun getChart(symbol: String, range: ChartRange): Result<ChartSeries?>
}
