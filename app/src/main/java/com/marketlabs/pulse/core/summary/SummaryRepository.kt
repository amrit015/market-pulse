package com.marketlabs.pulse.core.summary

import com.marketlabs.pulse.storage.model.summary.MarketPulse
import kotlinx.coroutines.flow.Flow

interface SummaryRepository {
    // 1. Passive Stream: Always returns whatever is in the DB (Fast, Offline-ready)
    fun getMarketPulseStream(): Flow<MarketPulse?>
    fun getDailyPulseStream(): Flow<MarketPulse?>

    // 2. Active Command: Fetches from API and updates DB
    // Returns Result so ViewModel can show errors (like "No Internet")
    suspend fun refreshMarketSummary(force: Boolean): Result<Unit>

    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}