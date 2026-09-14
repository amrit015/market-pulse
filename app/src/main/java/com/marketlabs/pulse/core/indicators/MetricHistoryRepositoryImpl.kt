package com.marketlabs.pulse.core.indicators

import android.util.Log
import com.marketlabs.pulse.core.sync.SyncManager
import com.marketlabs.pulse.network.store.indicators.RemoteMetricHistoryDataSource
import com.marketlabs.pulse.storage.model.indicators.MetricHistorySeries
import com.marketlabs.pulse.storage.store.indicators.LocalMetricHistoryDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MetricHistoryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalMetricHistoryDataSource,
    private val remoteDataSource: RemoteMetricHistoryDataSource,
    private val syncManager: SyncManager
) : MetricHistoryRepository {

    override fun getHistoryStream(metricId: String): Flow<MetricHistorySeries?> =
        localDataSource.getHistoryStream(metricId)

    override suspend fun refreshHistory(metricId: String, limit: Int?): Result<Unit> {
        if (isCacheFreshEnough(metricId)) {
            return Result.success(Unit)
        }

        return try {
            Log.d("MetricHistory", "🌐 Fetching history for $metricId...")

            remoteDataSource.getHistory(metricId, limit)
                .onSuccess { history -> history?.let { localDataSource.saveHistory(it) } }
                .onFailure { throw it }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MetricHistory", "Failed to refresh history for $metricId", e)
            Result.failure(e)
        }
    }

    /**
     * `indicator_charts_updated` only advances when `marketDataEngine.ts`'s
     * `saveIndicatorChartPoints()` actually wrote a new point for one of the 5 pillars -- one flag
     * for all of them, since they're all written in the same batch. A flag not yet present in
     * `SyncManager`'s map (never fired since this client attached the listener) is treated as
     * "not fresh," never as timestamp 0.
     */
    private suspend fun isCacheFreshEnough(metricId: String): Boolean {
        val cached = localDataSource.getHistoryStream(metricId).first() ?: return false
        val flagValue = syncManager.chartSyncTimestamps.value["indicator_charts_updated"] ?: return false
        return cached.lastSyncedTimestamp >= flagValue
    }
}
