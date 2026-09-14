package com.marketlabs.pulse.core.insights

import android.util.Log
import com.marketlabs.pulse.core.sync.SyncManager
import com.marketlabs.pulse.network.store.insights.RemoteInsightsHistoryDataSource
import com.marketlabs.pulse.storage.model.insights.InsightsHistorySeries
import com.marketlabs.pulse.storage.store.insights.LocalInsightsHistoryDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class InsightsHistoryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalInsightsHistoryDataSource,
    private val remoteDataSource: RemoteInsightsHistoryDataSource,
    private val syncManager: SyncManager
) : InsightsHistoryRepository {

    override fun getHistoryStream(metricId: String): Flow<InsightsHistorySeries?> =
        localDataSource.getHistoryStream(metricId)

    override suspend fun refreshHistory(metricId: String, limit: Int?): Result<Unit> {
        if (isCacheFreshEnough(metricId)) {
            return Result.success(Unit)
        }

        return try {
            Log.d("InsightsHistory", "🌐 Fetching history for $metricId...")

            remoteDataSource.getHistory(metricId, limit)
                .onSuccess { history -> history?.let { localDataSource.saveHistory(it) } }
                .onFailure { throw it }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("InsightsHistory", "Failed to refresh history for $metricId", e)
            Result.failure(e)
        }
    }

    /**
     * `InsightsHistoryPillar.forMetricId` (already the app's own POSTURE/POSITIONING classification
     * for these bare ids -- `"dark_pool_index"`, `"retail_sentiment"`, etc., no "posture."/
     * "positioning." prefix on `metricId` itself) picks which of the two flags applies; an
     * unrecognized id (`null` pillar) falls back to always-fetch rather than guessing. Each flag
     * only advances when that engine's `saveGaugeChartPoints()` actually wrote a new point. A flag
     * absent from `SyncManager`'s map (never fired since this client attached the listener) is
     * treated as "not fresh," never as timestamp 0.
     */
    private suspend fun isCacheFreshEnough(metricId: String): Boolean {
        val flagKey = when (InsightsHistoryPillar.forMetricId(metricId)) {
            InsightsHistoryPillar.POSTURE -> "posture_charts_updated"
            InsightsHistoryPillar.POSITIONING -> "positioning_charts_updated"
            null -> return false
        }
        val cached = localDataSource.getHistoryStream(metricId).first() ?: return false
        val flagValue = syncManager.chartSyncTimestamps.value[flagKey] ?: return false
        return cached.lastSyncedTimestamp >= flagValue
    }
}
