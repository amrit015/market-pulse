package com.marketlabs.pulse.core.positioning

import com.marketlabs.pulse.storage.model.positioning.DomainMarketPositioning
import kotlinx.coroutines.flow.Flow

interface MarketPositioningRepository {
    fun getPositioningStream(): Flow<DomainMarketPositioning?>
    suspend fun refreshPositioning(force: Boolean): Result<Unit>
    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}
