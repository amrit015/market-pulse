package com.marketlabs.pulse.core.indicators

import android.util.Log
import com.marketlabs.pulse.network.store.indicators.RemoteIndicatorsDataSource
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.storage.store.indicators.LocalIndicatorsDataSource
import com.marketlabs.pulse.utils.CachePolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class IndicatorsRepositoryImpl @Inject constructor(
    private val localDataSource: LocalIndicatorsDataSource,
    private val remoteDataSource: RemoteIndicatorsDataSource
) : IndicatorsRepository {

    override fun getIndicatorsStream(): Flow<MarketIndicators?> =
        localDataSource.getLatestCachedIndicators()

    override suspend fun refreshIndicators(force: Boolean): Result<Unit> {
        return try {
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("America/New_York")
            }.format(Date())

            val localData = localDataSource.getIndicatorsByDate(todayDateString).firstOrNull()
            val currentTime = System.currentTimeMillis()

            val shouldFetch = when {
                force -> true
                localData?.lastSyncedTimestamp == null -> true
                else -> CachePolicy.isHourlyExpired(localData.lastSyncedTimestamp, currentTime)
            }

            if (!shouldFetch) {
                Log.d("MarketIndicators", "✅ Three Pillars cache is fresh. Skipping network.")
                return Result.success(Unit)
            }

            Log.d("MarketIndicators", "🌐 Fetching latest Three Pillars from Firebase...")

            remoteDataSource.getLatestIndicators(dateId = todayDateString)
                .onSuccess { freshData ->
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