package com.marketlabs.pulse.storage.store.insights

import com.marketlabs.pulse.storage.model.insights.InsightsHistorySeries
import kotlinx.coroutines.flow.Flow

interface LocalInsightsHistoryDataSource {
    fun getHistoryStream(metricId: String): Flow<InsightsHistorySeries?>
    suspend fun saveHistory(history: InsightsHistorySeries)
}
