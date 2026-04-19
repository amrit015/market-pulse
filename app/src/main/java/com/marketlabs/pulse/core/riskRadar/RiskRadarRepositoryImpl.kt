package com.marketlabs.pulse.core.riskRadar

import android.util.Log
import com.marketlabs.pulse.network.store.riskRadar.RemoteRiskRadarDataSource
import com.marketlabs.pulse.storage.model.riskRadar.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import com.marketlabs.pulse.storage.store.riskRadar.LocalRiskRadarDataSource
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class RiskRadarRepositoryImpl @Inject constructor(
    private val localDataSource: LocalRiskRadarDataSource,
    private val remoteDataSource: RemoteRiskRadarDataSource
) : RiskRadarRepository {

    override fun getRiskStream(): Flow<RiskRadar?> = localDataSource.getLatestCachedRisk()

    override suspend fun refreshRisk(force: Boolean): Result<Unit> {
        return try {
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("America/New_York")
            }.format(Date())

            Log.d("RiskRadar", "🌐 Fetching latest Risk Radar from Firebase...")

            remoteDataSource.getLatestRisk(todayDateString).onSuccess { freshRisk ->
                localDataSource.saveRisk(freshRisk)
            }.onFailure {
                throw it
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RiskRadar", "Failed to refresh risk repository", e)
            Result.failure(e)
        }
    }

    override fun getTailRisksStream(): Flow<MarketRiskAssessment?> = localDataSource.getLatestCachedTailRisks()

    override suspend fun refreshTailRisks(force: Boolean): Result<Unit> {
        return try {
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("America/New_York")
            }.format(Date())

            Log.d("RiskRadar", "🌐 Fetching latest Tail Risks from Firebase...")

            remoteDataSource.getLatestTailRisks(todayDateString).onSuccess { freshRisks ->
                localDataSource.saveTailRisks(freshRisks)
            }.onFailure {
                throw it
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RiskRadar", "Failed to refresh tail risks repository", e)
            Result.failure(e)
        }
    }

    // ========================================================================
    // SYNC MANAGER TIMESTAMPS
    // ========================================================================

    override suspend fun getLastSyncedTimestampRisk(): Long? = localDataSource.getLastSyncedTimestampRisk()
    override suspend fun updateLastSyncedTimestampRisk(timestamp: Long) = localDataSource.updateLastSyncedTimestampRisk(timestamp)

    override suspend fun getLastSyncedTimestampTailRisks(): Long? = localDataSource.getLastSyncedTimestampTailRisks()
    override suspend fun updateLastSyncedTimestampTailRisks(timestamp: Long) = localDataSource.updateLastSyncedTimestampTailRisks(timestamp)
}