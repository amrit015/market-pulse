package com.marketlabs.pulse.core.posture

import com.marketlabs.pulse.storage.model.posture.DomainMarketPosture
import kotlinx.coroutines.flow.Flow

interface MarketPostureRepository {
    fun getPostureStream(): Flow<DomainMarketPosture?>
    suspend fun refreshPosture(force: Boolean): Result<Unit>
    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}