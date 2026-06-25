package com.marketlabs.pulse.storage.store.summary

import com.marketlabs.pulse.storage.database.dao.SummaryDao
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.mappers.toDomain
import com.marketlabs.pulse.storage.model.summary.mappers.toMarketPulseEntity
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

    override suspend fun clearAll() {
        dao.clearMarketPulse()
    }

    // ========================================================================
    // SYNC MANAGER TIMESTAMPS
    // ========================================================================

    override suspend fun getLastSyncedTimestamp(): Long? {
        return dao.getLastSyncedTimestamp()
    }

    override suspend fun updateLastSyncedTimestamp(timestamp: Long) {
        dao.updateLastSyncedTimestamp(timestamp)
    }
}