package com.marketlabs.pulse.core.charts

import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.model.charts.ChartSeries
import kotlinx.coroutines.flow.Flow

/**
 * On-demand, per-`(symbol, range)` — driven by `SyncManager`'s 5 chart flags (`charts_stocks_updated`,
 * `charts_equity_sector_updated`, `charts_sentiment_updated`, `charts_futures_commodities_updated`,
 * `charts_crypto_updated` on `system/sync_status`), one per backend batch that writes
 * `market_charts` together.
 *
 * `force = false` (every current caller) skips the network call entirely when the cached row's own
 * `lastSyncedTimestamp` is already at or past [chartSyncGroup]'s current flag value — that flag
 * only advances when its batch actually wrote a new point, so a cache already caught up to it is
 * provably current, no guessing at market hours required. `force = true` bypasses that check and
 * always hits the network. [chartSyncGroup] is `null` only for [ChartSyncGroup.fromAssetType]'s
 * `UNKNOWN` case, which falls back to always-fetch rather than guessing which flag applies.
 */
interface ChartsRepository {

    fun getChartStream(symbol: String, range: ChartRange): Flow<ChartSeries?>
    suspend fun refreshChart(
        symbol: String,
        range: ChartRange,
        force: Boolean = false,
        chartSyncGroup: ChartSyncGroup?
    ): Result<Unit>
}
