package com.marketlabs.pulse.storage.repository.summary

import com.marketlabs.pulse.storage.database.dao.MarketSummaryDao
import com.marketlabs.pulse.storage.database.entity.toDomain
import com.marketlabs.pulse.storage.database.entity.toEntity
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalMarketSummaryDataSourceImpl @Inject constructor(
    private val dao: MarketSummaryDao
) : LocalMarketSummaryDataSource {

    override fun getLatestMarketPulse(): Flow<MarketPulse?> {
        return dao.getLatestPulse().map { it?.toDomain() }
    }

    override suspend fun saveMarketPulse(pulse: MarketPulse) {
        dao.insertPulse(pulse.toEntity())
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}