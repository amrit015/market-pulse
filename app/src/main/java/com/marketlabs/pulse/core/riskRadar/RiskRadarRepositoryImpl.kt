package com.marketlabs.pulse.core.riskRadar

import android.util.Log
import com.marketlabs.pulse.network.store.riskRadar.RemoteRiskRadarDataSource
import com.marketlabs.pulse.storage.model.riskRadar.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import com.marketlabs.pulse.storage.store.riskRadar.LocalRiskRadarDataSource
import com.marketlabs.pulse.utils.CachePolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
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

            val localData = localDataSource.getRiskByDate(todayDateString).firstOrNull()
            val currentTime = System.currentTimeMillis()

            // caching policy
            val shouldFetch = when {
                force -> true
                localData?.lastSyncedTimestamp == null -> true
                else -> CachePolicy.isHourlyExpired(localData.lastSyncedTimestamp, currentTime)
            }

            if (!shouldFetch) {
                Log.d("RiskRadar", "✅ Risk cache is fresh (Current Hourly block). Skipping network.")
                return Result.success(Unit)
            }

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

            val localData = localDataSource.getTailRisksByDate(todayDateString).firstOrNull()
            val currentTime = System.currentTimeMillis()

            // Exact same caching policy as the gauges
            val shouldFetch = when {
                force -> true
                localData?.lastSyncedTimestamp == null -> true
                else -> CachePolicy.isHourlyExpired(localData.lastSyncedTimestamp, currentTime)
            }

            if (!shouldFetch) {
                Log.d("RiskRadar", "✅ Tail Risks cache is fresh. Skipping network.")
                return Result.success(Unit)
            }

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
}