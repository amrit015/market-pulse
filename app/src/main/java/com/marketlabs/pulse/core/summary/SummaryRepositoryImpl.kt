package com.marketlabs.pulse.core.summary

import android.util.Log
import com.marketlabs.pulse.network.store.summary.RemoteSummaryDataSource
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.store.summary.LocalSummaryDataSource
import com.marketlabs.pulse.utils.toDateIdString
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SummaryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalSummaryDataSource,
    private val remoteDataSource: RemoteSummaryDataSource
) : SummaryRepository {

    override fun getMarketPulseStream(): Flow<MarketPulse?> = localDataSource.getLatestMarketPulse()

    override suspend fun refreshMarketSummary(force: Boolean): Result<Unit> {
        return try {
            Log.d("MarketPulse", "🌐 Fetching latest Market Summary from Firebase...")

            // Fetch both V3 and V2.5 from the backend when a sync is triggered
            remoteDataSource.getLatestMarketPulse().onSuccess { freshV3 ->
                localDataSource.saveMarketPulse(freshV3)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MarketPulse", "Failed to fetch Market Summary", e)
            Result.failure(e)
        }
    }

    // ========================================================================
    // SYNC MANAGER TIMESTAMPS
    // ========================================================================

    override suspend fun getLastSyncedTimestamp(): Long? {
        return localDataSource.getLastSyncedTimestamp()
    }

    override suspend fun updateLastSyncedTimestamp(timestamp: Long) {
        localDataSource.updateLastSyncedTimestamp(timestamp)
    }

    // ========================================================================
    // CALENDAR STRIP (past days)
    // ========================================================================

    override fun getMarketPulseForDate(dateId: String): Flow<SummaryDateEntry> =
        localDataSource.getMarketPulseForDate(dateId)

    /**
     * The past-date caching rule (reduces Firestore reads -- a past day's report never changes
     * again once the backend is done writing it for that day):
     *   - No cached row yet for [dateId] -> fetch once. A 404 (no report exists for that date)
     *     writes a tombstone so this date is never queried again.
     *   - Cached row exists, and it was last written on the SAME NY calendar day as [dateId] ->
     *     refetch once more (the backend may still post a later same-day revision -- e.g. the user
     *     opened yesterday's report at 4pm, but the day's report kept revising until 9pm), then the
     *     new write timestamp locks it in.
     *   - Cached row exists, last written on an NY calendar day AFTER [dateId] -> confirmed final,
     *     never refetch. This is the state a same-day refetch above lands in on its next visit.
     * All comparisons are anchored to NY time ([toDateIdString], not the device's timezone) --
     * doing this with the device's local date would misfire for a viewer far enough ahead of NY
     * (e.g. already "tomorrow" locally while NY is still finishing today), permanently locking in
     * a same-day cache before the day's actual final report had even been written.
     */
    override suspend fun syncPastDate(dateId: String): Result<Unit> {
        return try {
            val lastSyncedTimestamp = localDataSource.getLastSyncedTimestampForDate(dateId)
            if (lastSyncedTimestamp != null && lastSyncedTimestamp.toDateIdString() != dateId) {
                // Written on an NY day after dateId -- final, nothing to do.
                return Result.success(Unit)
            }

            remoteDataSource.getMarketPulseByDate(dateId).fold(
                onSuccess = { fresh ->
                    if (fresh != null) {
                        localDataSource.saveMarketPulse(fresh)
                    } else {
                        localDataSource.saveTombstone(dateId)
                    }
                    Result.success(Unit)
                },
                onFailure = { e -> Result.failure(e) }
            )
        } catch (e: Exception) {
            Log.e("MarketPulse", "Failed to sync pulse for $dateId", e)
            Result.failure(e)
        }
    }
}