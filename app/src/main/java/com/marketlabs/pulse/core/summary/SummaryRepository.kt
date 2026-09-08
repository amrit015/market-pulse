package com.marketlabs.pulse.core.summary

import com.marketlabs.pulse.storage.model.summary.MarketPulse
import kotlinx.coroutines.flow.Flow

interface SummaryRepository {
    // 1. Passive Stream: Always returns whatever is in the DB (Fast, Offline-ready)
    fun getMarketPulseStream(): Flow<MarketPulse?>

    // 2. Active Command: Fetches from API and updates DB
    // Returns Result so ViewModel can show errors (like "No Internet")
    suspend fun refreshMarketSummary(force: Boolean): Result<Unit>

    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)

    // ========================================================================
    // CALENDAR STRIP (past days -- see syncPastDate's doc comment on the impl for the caching
    // rule). Today never goes through these -- it stays on the passive/active pair above, driven
    // by SyncManager.
    // ========================================================================

    /** Reactive read of a specific past date's cached entry (or lack of one). */
    fun getMarketPulseForDate(dateId: String): Flow<SummaryDateEntry>

    /** Applies the past-date caching rule for [dateId], fetching from Firestore only if needed. */
    suspend fun syncPastDate(dateId: String): Result<Unit>
}