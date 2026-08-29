package com.marketlabs.pulse.storage.store.positioning

import com.marketlabs.pulse.storage.database.dao.MarketPositioningDao
import com.marketlabs.pulse.storage.model.positioning.DomainMarketPositioning
import com.marketlabs.pulse.storage.model.positioning.mappers.toDomain
import com.marketlabs.pulse.storage.model.positioning.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalMarketPositioningDataSourceImpl @Inject constructor(
    private val dao: MarketPositioningDao
) : LocalMarketPositioningDataSource {

    override fun getLatestPositioningStream(): Flow<DomainMarketPositioning?> {
        return dao.getMarketPositioningStream().map { it?.toDomain() }
    }

    override suspend fun savePositioning(positioning: DomainMarketPositioning) {
        dao.insertMarketPositioning(positioning.toEntity())
    }

    override suspend fun getLastSyncedTimestamp(): Long? {
        return dao.getLastSyncedTimestamp()
    }

    override suspend fun updateLastSyncedTimestamp(timestamp: Long) {
        dao.updateLastSyncedTimestamp(timestamp)
    }
}
