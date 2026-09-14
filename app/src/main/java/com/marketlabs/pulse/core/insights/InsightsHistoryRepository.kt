package com.marketlabs.pulse.core.insights

import com.marketlabs.pulse.storage.model.insights.InsightsHistorySeries
import kotlinx.coroutines.flow.Flow

/** On-demand, per-`metricId` -- gated on `system/sync_status`'s `posture_charts_updated`/
 *  `positioning_charts_updated` flags (see `InsightsHistoryRepositoryImpl`), same principle as
 *  Indicators' `MetricHistoryRepository`. */
interface InsightsHistoryRepository {

    fun getHistoryStream(metricId: String): Flow<InsightsHistorySeries?>
    suspend fun refreshHistory(metricId: String, limit: Int? = null): Result<Unit>
}
