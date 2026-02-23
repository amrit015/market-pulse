package com.marketlabs.pulse.core.summary

import android.util.Log
import com.marketlabs.pulse.network.store.summary.RemoteSummaryDataSource
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.repository.summary.LocalSummaryDataSource
import com.marketlabs.pulse.utils.getMidnightTimestamp
import com.marketlabs.pulse.utils.getTodayDateString
import com.marketlabs.pulse.utils.getYesterdayDateString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SummaryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalSummaryDataSource,
    private val remoteDataSource: RemoteSummaryDataSource
) : SummaryRepository {

    // Expose both streams to the ViewModel
    override fun getMarketPulseStream(): Flow<MarketPulse?> = localDataSource.getLatestMarketPulse()
    override fun getDailyPulseStream(): Flow<MarketPulse?> = localDataSource.getLatestDailyPulse()

    override suspend fun refreshMarketSummary(force: Boolean): Result<Unit> {
        return try {
            val localData = localDataSource.getLatestMarketPulse().firstOrNull()
            val currentTime = System.currentTimeMillis()

            val todayDateId = getTodayDateString()
            val yesterdayDateId = getYesterdayDateString()
            val midnightToday = getMidnightTimestamp()

            val shouldFetch = when {
                force -> true
                localData == null -> true
                localData.dateId == todayDateId -> {
                    // It's today's data. Update automatically if it's been more than 60 minutes.
                    val lastSync = localData.lastSyncedTimestamp ?: 0L
                    val timeSinceLastSync = currentTime - lastSync
                    timeSinceLastSync > (60 * 60 * 1000)
                }
                localData.dateId == yesterdayDateId -> {
                    // It's yesterday's data. Fetch if we have crossed midnight into a new day.
                    localData.lastSyncedTimestamp?.let { it < midnightToday } ?: true
                }
                else -> true // The data is older than yesterday, definitely fetch.
            }

            if (!shouldFetch) {
                Log.d("MarketPulse", "✅ Summary cache is fresh (< 60 mins). Skipping network.")
                return Result.success(Unit)
            }

            Log.d("MarketPulse", "🌐 Fetching latest Market Summary from Firebase...")

            // 👇 FETCH BOTH REPORTS FROM FIREBASE
            remoteDataSource.getLatestMarketPulse().onSuccess { freshV3 ->
                // Ensure we tag the fresh data with the exact sync time so the 60 min timer resets
                val dataToSave = freshV3.copy(lastSyncedTimestamp = currentTime)
                localDataSource.saveMarketPulse(dataToSave)
            }

            remoteDataSource.getLatestDailyPulse().onSuccess { freshV2 ->
                val dataToSave = freshV2.copy(lastSyncedTimestamp = currentTime)
                localDataSource.saveDailyPulse(dataToSave)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MarketPulse", "Failed to fetch Market Summary", e)
            Result.failure(e)
        }
    }
}