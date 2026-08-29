package com.marketlabs.pulse.storage.store.positioning

import com.marketlabs.pulse.storage.model.positioning.DomainMarketPositioning
import kotlinx.coroutines.flow.Flow

interface LocalMarketPositioningDataSource {
    fun getLatestPositioningStream(): Flow<DomainMarketPositioning?>
    suspend fun savePositioning(positioning: DomainMarketPositioning)
    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}
