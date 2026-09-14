package com.marketlabs.pulse.network.store.insights

import android.util.Log
import com.marketlabs.pulse.core.insights.InsightsHistoryPillar
import com.marketlabs.pulse.network.api.MarketPositioningApi
import com.marketlabs.pulse.network.api.MarketPostureApi
import com.marketlabs.pulse.storage.model.insights.InsightsHistorySeries
import com.marketlabs.pulse.storage.model.insights.mappers.toDomain
import javax.inject.Inject

/**
 * Routes to the right of the 2 `.../history` endpoints via `InsightsHistoryPillar.forMetricId`,
 * since the pillar isn't part of the metric id itself -- same reasoning as Indicators'
 * `RemoteMetricHistoryDataSourceImpl`, just spanning 2 separate `Api` interfaces (`MarketPostureApi`/
 * `MarketPositioningApi`) instead of routing within one shared `IndicatorsApi`, matching how this
 * app already keeps Posture/Positioning's live-snapshot APIs as two distinct interfaces.
 *
 * An id outside both sets resolves to `null` and this returns `Result.success(null)` without a
 * network call. An empty response array is a normal, successful "no data yet" result, mapped
 * straight through to an empty-points `InsightsHistorySeries`, same as Indicators.
 */
class RemoteInsightsHistoryDataSourceImpl @Inject constructor(
    private val postureApi: MarketPostureApi,
    private val positioningApi: MarketPositioningApi
) : RemoteInsightsHistoryDataSource {

    override suspend fun getHistory(metricId: String, limit: Int?): Result<InsightsHistorySeries?> {
        val pillar = InsightsHistoryPillar.forMetricId(metricId) ?: return Result.success(null)

        return try {
            val syncTimestamp = System.currentTimeMillis()
            val response = when (pillar) {
                InsightsHistoryPillar.POSTURE -> postureApi.getPostureHistory(metricId, limit)
                InsightsHistoryPillar.POSITIONING -> positioningApi.getPositioningHistory(metricId, limit)
            }
            Result.success(response.toDomain(metricId = metricId, lastSyncedTimestamp = syncTimestamp))
        } catch (e: Exception) {
            Log.e("InsightsHistory", "Failed to fetch history for $metricId", e)
            Result.failure(e)
        }
    }
}
