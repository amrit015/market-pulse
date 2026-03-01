package com.marketlabs.pulse.core.indicators

import android.util.Log
import com.marketlabs.pulse.network.store.indicators.RemoteIndicatorsDataSource
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.storage.store.indicators.LocalIndicatorsDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class IndicatorsRepositoryImpl @Inject constructor(
    private val localDataSource: LocalIndicatorsDataSource,
    private val remoteDataSource: RemoteIndicatorsDataSource
) : IndicatorsRepository {

    override fun getIndicatorsStream(): Flow<MarketIndicators?> = localDataSource.getLatestCachedIndicators()

    override suspend fun refreshIndicators(force: Boolean): Result<Unit> {
        return try {
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("America/New_York")
            }.format(Date())

            val localData = localDataSource.getIndicatorsByDate(todayDateString).firstOrNull()
            val currentTime = System.currentTimeMillis()

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"))
            val isWeekend = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

            val shouldFetch = when {
                force -> true
                localData == null -> true
                isWeekend -> false
                else -> {
                    val lastSync = localData.lastSyncedTimestamp ?: 0L
                    val timeSinceLastSync = currentTime - lastSync
                    timeSinceLastSync > (60 * 60 * 1000) // 60 minutes TTL
                }
            }

            if (!shouldFetch) {
                Log.d("MarketIndicators", "✅ Indicators cache is fresh (or weekend). Skipping network.")
                return Result.success(Unit)
            }

            Log.d("MarketIndicators", "🌐 Fetching latest Traffic Light Indicators from Firebase...")

            // Fetch the combined Domain object from the Remote Data Source
            remoteDataSource.getLatestIndicators(
                dateId = todayDateString,
                timestamp = currentTime
            ).onSuccess { freshData ->
                localDataSource.saveIndicators(freshData)
            }.onFailure {
                throw it
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MarketIndicators", "Failed to refresh indicators repository", e)
            Result.failure(e)
        }
    }
}