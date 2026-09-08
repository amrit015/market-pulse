package com.marketlabs.pulse.storage.store.summary

import com.marketlabs.pulse.core.summary.SummaryDateEntry
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import kotlinx.coroutines.flow.Flow

interface LocalSummaryDataSource {

    fun getLatestMarketPulse(): Flow<MarketPulse?>
    suspend fun saveMarketPulse(pulse: MarketPulse)

    suspend fun clearAll()

    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)

    // --- Calendar strip (past-day lookups by dateId) ---

    fun getMarketPulseForDate(dateId: String): Flow<SummaryDateEntry>
    suspend fun getLastSyncedTimestampForDate(dateId: String): Long?

    /** Marks [dateId] as confirmed to have no report -- never re-fetched again. */
    suspend fun saveTombstone(dateId: String, lastSyncedTimestamp: Long = System.currentTimeMillis())
}