package com.marketlabs.pulse.core.summary

import android.util.Log
import com.marketlabs.pulse.network.store.summary.RemoteSummaryDataSource
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.store.summary.LocalSummaryDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SummaryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalSummaryDataSource,
    private val remoteDataSource: RemoteSummaryDataSource
) : SummaryRepository {

    override fun getMarketPulseStream(): Flow<MarketPulse?> = localDataSource.getLatestMarketPulse()
    override fun getDailyPulseStream(): Flow<MarketPulse?> = localDataSource.getLatestDailyPulse()

    override suspend fun refreshMarketSummary(force: Boolean): Result<Unit> {
        return try {
            Log.d("MarketPulse", "🌐 Fetching latest Market Summary from Firebase...")

            // Fetch both V3 and V2.5 from the backend when a sync is triggered
            remoteDataSource.getLatestMarketPulse().onSuccess { freshV3 ->
                localDataSource.saveMarketPulse(freshV3)
            }

            remoteDataSource.getLatestDailyPulse().onSuccess { freshV2 ->
                localDataSource.saveDailyPulse(freshV2)
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
}