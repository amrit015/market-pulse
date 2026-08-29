package com.marketlabs.pulse.network.store.indicators

import android.util.Log
import com.marketlabs.pulse.core.indicators.MetricHistoryPillar
import com.marketlabs.pulse.network.api.IndicatorsApi
import com.marketlabs.pulse.storage.model.indicators.MetricHistorySeries
import com.marketlabs.pulse.storage.model.indicators.mappers.toDomain
import javax.inject.Inject

/**
 * Routes to the right of the 4 `.../history` endpoints via `MetricHistoryPillar.forMetricId`,
 * since the pillar isn't part of the metric id itself. An id outside all four sets (e.g.
 * `positioning`'s or `ai_synthesis`'s fields, neither of which is chartable per the spec) resolves
 * to `null` and this returns `Result.success(null)` without a network call -- not an error, just
 * "no history endpoint exists for this."
 *
 * An empty response array is a normal, successful "no data yet for this metric" per the spec
 * (`[]`, not a 404) -- mapped straight through to an empty-points `MetricHistorySeries`, same as
 * any other successful fetch.
 */
class RemoteMetricHistoryDataSourceImpl @Inject constructor(
    private val api: IndicatorsApi
) : RemoteMetricHistoryDataSource {

    override suspend fun getHistory(metricId: String, limit: Int?): Result<MetricHistorySeries?> {
        val pillar = MetricHistoryPillar.forMetricId(metricId) ?: return Result.success(null)

        return try {
            val syncTimestamp = System.currentTimeMillis()
            val response = when (pillar) {
                MetricHistoryPillar.TACTICAL_MOMENTUM -> api.getTacticalMomentumHistory(metricId, limit)
                MetricHistoryPillar.SYSTEMIC_RISK -> api.getSystemicRiskHistory(metricId, limit)
                MetricHistoryPillar.VALUATION -> api.getValuationHistory(metricId, limit)
                MetricHistoryPillar.MACRO_VITALS -> api.getMacroVitalsHistory(metricId, limit)
            }
            Result.success(response.toDomain(metricId = metricId, lastSyncedTimestamp = syncTimestamp))
        } catch (e: Exception) {
            Log.e("MetricHistory", "Failed to fetch history for $metricId", e)
            Result.failure(e)
        }
    }
}
