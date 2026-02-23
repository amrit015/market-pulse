package com.marketlabs.pulse.storage.repository.summary

import com.marketlabs.pulse.storage.database.dao.SummaryDao
import com.marketlabs.pulse.storage.database.entity.toDailyPulseEntity
import com.marketlabs.pulse.storage.database.entity.toDomain
import com.marketlabs.pulse.storage.database.entity.toMarketPulseEntity
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalSummaryDataSourceImpl @Inject constructor(
    private val dao: SummaryDao
) : LocalSummaryDataSource {

    // V3 Stream
    override fun getLatestMarketPulse(): Flow<MarketPulse?> {
        return dao.getLatestMarketPulse().map { it?.toDomain() }
    }

    override suspend fun saveMarketPulse(pulse: MarketPulse) {
        dao.insertMarketPulse(pulse.toMarketPulseEntity())
    }

    // V2.5 Stream
    override fun getLatestDailyPulse(): Flow<MarketPulse?> {
        return dao.getLatestDailyPulse().map { it?.toDomain() }
    }

    override suspend fun saveDailyPulse(pulse: MarketPulse) {
        dao.insertDailyPulse(pulse.toDailyPulseEntity())
    }

    override suspend fun clearAll() {
        dao.clearMarketPulse()
        dao.clearDailyPulse()
    }
}