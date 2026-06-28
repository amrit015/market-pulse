package com.marketlabs.pulse.storage.store.posture

import com.marketlabs.pulse.storage.model.posture.DomainMarketPosture
import kotlinx.coroutines.flow.Flow

interface LocalMarketPostureDataSource {
    fun getLatestPostureStream(): Flow<DomainMarketPosture?>
    suspend fun savePosture(posture: DomainMarketPosture)
    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}