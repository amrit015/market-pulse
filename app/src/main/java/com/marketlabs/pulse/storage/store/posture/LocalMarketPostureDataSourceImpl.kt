package com.marketlabs.pulse.storage.store.posture

import com.marketlabs.pulse.storage.database.dao.MarketPostureDao
import com.marketlabs.pulse.storage.model.posture.DomainMarketPosture
import com.marketlabs.pulse.storage.model.posture.mappers.toDomain
import com.marketlabs.pulse.storage.model.posture.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalMarketPostureDataSourceImpl @Inject constructor(
    private val dao: MarketPostureDao
) : LocalMarketPostureDataSource {

    override fun getLatestPostureStream(): Flow<DomainMarketPosture?> {
        return dao.getMarketPostureStream().map { it?.toDomain() }
    }

    override suspend fun savePosture(posture: DomainMarketPosture) {
        dao.insertMarketPosture(posture.toEntity())
    }

    /**
     * Retrieves the last synced timestamp. Returns null if posture data has not been saved yet.
     */
    override suspend fun getLastSyncedTimestamp(): Long? {
        return dao.getLastSyncedTimestamp()
    }

    /**
     * Updates the last synced timestamp in the local database.
     */
    override suspend fun updateLastSyncedTimestamp(timestamp: Long) {
        dao.updateLastSyncedTimestamp(timestamp)
    }
}