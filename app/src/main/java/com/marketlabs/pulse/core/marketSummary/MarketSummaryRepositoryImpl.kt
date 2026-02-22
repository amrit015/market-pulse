package com.marketlabs.pulse.core.marketSummary

import android.util.Log
import com.marketlabs.pulse.network.store.summary.RemoteMarketSummaryDataSource
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.repository.summary.LocalMarketSummaryDataSource
import com.marketlabs.pulse.utils.getMidnightTimestamp
import com.marketlabs.pulse.utils.getTodayDateString
import com.marketlabs.pulse.utils.getYesterdayDateString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class MarketSummaryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalMarketSummaryDataSource,
    private val remoteDataSource: RemoteMarketSummaryDataSource
) : MarketSummaryRepository {

    // Expose both streams to the ViewModel
    override fun getMarketPulseStream(): Flow<MarketPulse?> = localDataSource.getLatestMarketPulse()
    override fun getDailyPulseStream(): Flow<MarketPulse?> = localDataSource.getLatestDailyPulse()

    override suspend fun refreshMarketSummary(force: Boolean): Result<Unit> {
        return try {
            val localData = localDataSource.getLatestMarketPulse().firstOrNull()

            val todayDateId = getTodayDateString()
            val yesterdayDateId = getYesterdayDateString()
            val midnightToday = getMidnightTimestamp()

            val shouldFetch = when {
                force -> true
                localData == null -> true
                localData.dateId == todayDateId -> true
                localData.dateId == yesterdayDateId -> {
                    localData.lastSyncedTimestamp?.let { it < midnightToday } ?: true
                }
                else -> false
            }

            if (!shouldFetch) return Result.success(Unit)

            // 👇 FETCH BOTH REPORTS FROM FIREBASE
            // (Make sure to add getLatestDailyPulse to your remoteDataSource!)
            remoteDataSource.getLatestMarketPulse().onSuccess { freshV3 ->
                localDataSource.saveMarketPulse(freshV3)
            }

            remoteDataSource.getLatestDailyPulse().onSuccess { freshV2 ->
                localDataSource.saveDailyPulse(freshV2)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MarketPulse", "Failed to fetch", e)
            Result.failure(e)
        }
    }
}