package com.marketlabs.pulse.network.store.indicators

import com.marketlabs.pulse.storage.model.indicators.MetricHistorySeries

interface RemoteMetricHistoryDataSource {
    suspend fun getHistory(metricId: String, limit: Int? = null): Result<MetricHistorySeries?>
}
