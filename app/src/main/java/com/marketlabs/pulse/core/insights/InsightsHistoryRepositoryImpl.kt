package com.marketlabs.pulse.core.insights

import android.util.Log
import com.marketlabs.pulse.network.store.insights.RemoteInsightsHistoryDataSource
import com.marketlabs.pulse.storage.model.insights.InsightsHistorySeries
import com.marketlabs.pulse.storage.store.insights.LocalInsightsHistoryDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InsightsHistoryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalInsightsHistoryDataSource,
    private val remoteDataSource: RemoteInsightsHistoryDataSource
) : InsightsHistoryRepository {

    override fun getHistoryStream(metricId: String): Flow<InsightsHistorySeries?> =
        localDataSource.getHistoryStream(metricId)

    override suspend fun refreshHistory(metricId: String, limit: Int?): Result<Unit> {
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
}
