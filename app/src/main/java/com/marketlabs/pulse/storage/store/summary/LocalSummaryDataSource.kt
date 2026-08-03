package com.marketlabs.pulse.storage.store.summary

import com.marketlabs.pulse.storage.model.summary.MarketPulse
import kotlinx.coroutines.flow.Flow

interface LocalSummaryDataSource {

    fun getLatestMarketPulse(): Flow<MarketPulse?>
    suspend fun saveMarketPulse(pulse: MarketPulse)

    suspend fun clearAll()

    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}