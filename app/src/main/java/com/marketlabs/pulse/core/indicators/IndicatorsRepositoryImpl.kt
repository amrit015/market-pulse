package com.marketlabs.pulse.core.indicators

import android.util.Log
import com.marketlabs.pulse.network.store.indicators.RemoteIndicatorsDataSource
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.storage.store.indicators.LocalIndicatorsDataSource
import kotlinx.coroutines.flow.Flow
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

    /**
     * Refreshes the indicators from the network.
     * Note: Cache expiration logic has been removed. This method is now strictly
     * driven by the SyncManager or explicit user pull-to-refresh actions.
     */
    override suspend fun refreshIndicators(force: Boolean): Result<Unit> {
        return try {
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("America/New_York")
            }.format(Date())

            // 💡 Updated log to reflect the new architecture
            Log.d("MarketIndicators", "🌐 Fetching latest Market Indicators (4 Pillars + AI) from Firebase...")

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

    /**
     * Retrieves the timestamp of the last successful sync from the local cache.
     */
    override suspend fun getLastSyncedTimestamp(): Long? {
        return localDataSource.getLastSyncedTimestamp()
    }

    /**
     * Updates the local cache with the latest sync timestamp.
     */
    override suspend fun updateLastSyncedTimestamp(timestamp: Long) {
        localDataSource.updateLastSyncedTimestamp(timestamp)
    }
}