package com.marketlabs.pulse.core.indicators

import android.util.Log
import com.marketlabs.pulse.network.store.indicators.RemoteMetricHistoryDataSource
import com.marketlabs.pulse.storage.model.indicators.MetricHistorySeries
import com.marketlabs.pulse.storage.store.indicators.LocalMetricHistoryDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MetricHistoryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalMetricHistoryDataSource,
    private val remoteDataSource: RemoteMetricHistoryDataSource
) : MetricHistoryRepository {

    override fun getHistoryStream(metricId: String): Flow<MetricHistorySeries?> =
        localDataSource.getHistoryStream(metricId)

    override suspend fun refreshHistory(metricId: String, limit: Int?): Result<Unit> {
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
}
