package com.marketlabs.pulse.core.marketRisk

import android.util.Log
import com.marketlabs.pulse.network.store.marketRisk.RemoteMarketRiskDataSource
import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskAssessment
import com.marketlabs.pulse.storage.store.marketRisk.LocalMarketRiskDataSource
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class MarketRiskRepositoryImpl @Inject constructor(
    private val localDataSource: LocalMarketRiskDataSource,
    private val remoteDataSource: RemoteMarketRiskDataSource
) : MarketRiskRepository {

    override fun getTailRisksStream(): Flow<MarketRiskAssessment?> = localDataSource.getLatestCachedTailRisks()

    override suspend fun refreshTailRisks(force: Boolean): Result<Unit> {
        return try {
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("America/New_York")
            }.format(Date())

            Log.d("MarketRisk", "🌐 Fetching latest Tail Risks from Firebase...")

            remoteDataSource.getLatestTailRisks(todayDateString).onSuccess { freshRisks ->
                localDataSource.saveTailRisks(freshRisks)
            }.onFailure {
                throw it
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MarketRisk", "Failed to refresh tail risks repository", e)
            Result.failure(e)
        }
    }

    // ========================================================================
    // SYNC MANAGER TIMESTAMPS
    // ========================================================================

    override suspend fun getLastSyncedTimestampTailRisks(): Long? = localDataSource.getLastSyncedTimestampTailRisks()
    override suspend fun updateLastSyncedTimestampTailRisks(timestamp: Long) = localDataSource.updateLastSyncedTimestampTailRisks(timestamp)
}