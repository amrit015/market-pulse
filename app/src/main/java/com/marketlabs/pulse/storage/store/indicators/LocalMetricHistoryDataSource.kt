package com.marketlabs.pulse.storage.store.indicators

import com.marketlabs.pulse.storage.model.indicators.MetricHistorySeries
import kotlinx.coroutines.flow.Flow

interface LocalMetricHistoryDataSource {
    fun getHistoryStream(metricId: String): Flow<MetricHistorySeries?>
    suspend fun saveHistory(history: MetricHistorySeries)
}
