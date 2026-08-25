package com.marketlabs.pulse.core.charts

import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.model.charts.ChartSeries
import kotlinx.coroutines.flow.Flow

/**
 * On-demand, per-`(symbol, range)` — not `SyncManager`-driven. No `charts_updated`/
 * `dashboard_updated` sync flag is wired in this app yet, and these charts only move once a day
 * (one EOD write per symbol), so refetching on screen focus is enough without adding one. `force
 * = true` is what a range-picker tap or pull-to-refresh should pass; `force = false` is a screen's
 * own first-paint pre-warm, mirroring `StockAnalysisRepository`'s shape.
 */
interface ChartsRepository {

    fun getChartStream(symbol: String, range: ChartRange): Flow<ChartSeries?>
    suspend fun refreshChart(symbol: String, range: ChartRange, force: Boolean = false): Result<Unit>
}
