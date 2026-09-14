package com.marketlabs.pulse.core.insights

import com.marketlabs.pulse.storage.model.insights.InsightsHistorySeries
import kotlinx.coroutines.flow.Flow

/** On-demand, per-`metricId` -- same reasoning as Indicators' `MetricHistoryRepository`: no sync flag for this domain, refetching on the glossary-detail page's own focus is enough. */
interface InsightsHistoryRepository {

    fun getHistoryStream(metricId: String): Flow<InsightsHistorySeries?>
    suspend fun refreshHistory(metricId: String, limit: Int? = null): Result<Unit>
}
