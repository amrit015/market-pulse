package com.marketlabs.pulse.storage.store.indicators

import com.marketlabs.pulse.storage.database.dao.MetricHistoryDao
import com.marketlabs.pulse.storage.model.indicators.MetricHistorySeries
import com.marketlabs.pulse.storage.model.indicators.mappers.toDomain
import com.marketlabs.pulse.storage.model.indicators.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalMetricHistoryDataSourceImpl @Inject constructor(
    private val dao: MetricHistoryDao
) : LocalMetricHistoryDataSource {

    override fun getHistoryStream(metricId: String): Flow<MetricHistorySeries?> =
        dao.getHistoryStream(metricId).map { entity -> entity?.toDomain() }

    override suspend fun saveHistory(history: MetricHistorySeries) {
        dao.insertHistory(history.toEntity())
    }
}
