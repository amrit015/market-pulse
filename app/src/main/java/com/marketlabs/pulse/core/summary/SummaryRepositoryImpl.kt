package com.marketlabs.pulse.core.summary

import android.util.Log
import com.marketlabs.pulse.network.store.summary.RemoteSummaryDataSource
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.store.summary.LocalSummaryDataSource
import com.marketlabs.pulse.utils.CachePolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SummaryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalSummaryDataSource,
    private val remoteDataSource: RemoteSummaryDataSource
) : SummaryRepository {

    override fun getMarketPulseStream(): Flow<MarketPulse?> = localDataSource.getLatestMarketPulse()
    override fun getDailyPulseStream(): Flow<MarketPulse?> = localDataSource.getLatestDailyPulse()

    override suspend fun refreshMarketSummary(force: Boolean): Result<Unit> {
        return try {
            val localData = localDataSource.getLatestMarketPulse().firstOrNull()

            // caching logic
            val shouldFetch = when {
                force -> true
                localData?.lastSyncedTimestamp == null -> true
                else -> CachePolicy.isHourlyExpired(localData.lastSyncedTimestamp)
            }

            if (!shouldFetch) {
                Log.d("MarketPulse", "✅ Summary cache is fresh (Current Hourly block). Skipping network.")
                return Result.success(Unit)
            }

            Log.d("MarketPulse", "🌐 Fetching latest Market Summary from Firebase...")

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
}