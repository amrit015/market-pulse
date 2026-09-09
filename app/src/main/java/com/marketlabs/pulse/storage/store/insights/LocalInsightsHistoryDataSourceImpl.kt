package com.marketlabs.pulse.storage.store.insights

import com.marketlabs.pulse.storage.database.dao.InsightsHistoryDao
import com.marketlabs.pulse.storage.model.insights.InsightsHistorySeries
import com.marketlabs.pulse.storage.model.insights.mappers.toDomain
import com.marketlabs.pulse.storage.model.insights.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalInsightsHistoryDataSourceImpl @Inject constructor(
    private val dao: InsightsHistoryDao
) : LocalInsightsHistoryDataSource {

    override fun getHistoryStream(metricId: String): Flow<InsightsHistorySeries?> =
        dao.getHistoryStream(metricId).map { entity -> entity?.toDomain() }

    override suspend fun saveHistory(history: InsightsHistorySeries) {
        dao.insertHistory(history.toEntity())
    }
}
