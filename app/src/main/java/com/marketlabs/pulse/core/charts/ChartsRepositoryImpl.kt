package com.marketlabs.pulse.core.charts

import android.util.Log
import com.marketlabs.pulse.core.sync.SyncManager
import com.marketlabs.pulse.network.store.charts.RemoteChartDataSource
import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.store.charts.LocalChartDataSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ChartsRepositoryImpl @Inject constructor(
    private val localDataSource: LocalChartDataSource,
    private val remoteDataSource: RemoteChartDataSource,
    private val syncManager: SyncManager
) : ChartsRepository {

    override fun getChartStream(symbol: String, range: ChartRange) =
        localDataSource.getChartStream(symbol, range)

    override suspend fun refreshChart(
        symbol: String,
        range: ChartRange,
        force: Boolean,
        chartSyncGroup: ChartSyncGroup?
    ): Result<Unit> {
        if (!force && chartSyncGroup != null && isCacheFreshEnough(symbol, range, chartSyncGroup)) {
            return Result.success(Unit)
        }

        return try {
            Log.d("Charts", "🌐 Fetching $range chart for $symbol...")

            remoteDataSource.getChart(symbol, range)
                .onSuccess { chart -> chart?.let { localDataSource.saveChart(it) } }
                .onFailure { throw it }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Charts", "Failed to refresh $range chart for $symbol", e)
            Result.failure(e)
        }
    }

    /**
     * A cached row is provably current once it was synced at or after [chartSyncGroup]'s flag --
     * that flag only advances when its backend batch actually wrote a new point (see
     * `SyncManager.chartSyncTimestamps`'s doc comment), so this needs no market-hours guessing.
     * A flag absent from the map (not yet fired since this client attached the listener) is treated
     * as "not fresh" -- always fetch -- never as timestamp 0, which would look "fresher than
     * anything" and wrongly suppress every refetch until the flag first fires.
     */
    private suspend fun isCacheFreshEnough(symbol: String, range: ChartRange, chartSyncGroup: ChartSyncGroup): Boolean {
        val cached = localDataSource.getChartStream(symbol, range).first() ?: return false
        val flagValue = syncManager.chartSyncTimestamps.value[chartSyncGroup.flagKey] ?: return false
        return cached.lastSyncedTimestamp >= flagValue
    }
}
