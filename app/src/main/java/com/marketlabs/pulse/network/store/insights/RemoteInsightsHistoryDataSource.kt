package com.marketlabs.pulse.network.store.insights

import com.marketlabs.pulse.storage.model.insights.InsightsHistorySeries

interface RemoteInsightsHistoryDataSource {
    suspend fun getHistory(metricId: String, limit: Int?): Result<InsightsHistorySeries?>
}
