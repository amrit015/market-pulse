package com.marketlabs.pulse.core.riskRadar

import android.util.Log
import com.marketlabs.pulse.network.store.riskRadar.RemoteRiskRadarDataSource
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import com.marketlabs.pulse.storage.repository.riskRadar.LocalRiskRadarDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
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

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"))
            val isWeekend = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

            // 💡 UPDATED LOGIC
            val shouldFetch = when {
                force -> true
                localData == null -> true // Always fetch if we don't have today's cache yet (fixes the weekend gap!)
                isWeekend -> false // If it's the weekend AND we already fetched today, lock it down.
                else -> {
                    // It's a weekday AND we have today's cache. Update every 60 minutes.
                    val timeSinceLastSync = currentTime - localData.lastSyncedTimestamp
                    timeSinceLastSync > (60 * 60 * 1000)
                }
            }

            if (!shouldFetch) {
                Log.d("RiskRadar", "✅ Risk cache is fresh (or weekend fetch complete). Skipping network.")
                return Result.success(Unit)
            }

            Log.d("RiskRadar", "🌐 Fetching latest Risk Radar from Firebase...")

            remoteDataSource.getLatestRisk().onSuccess { freshRisk ->
                val riskToSave = freshRisk.copy(
                    date = todayDateString,
                    lastSyncedTimestamp = currentTime
                )
                localDataSource.saveRisk(riskToSave)
            }.onFailure {
                throw it
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RiskRadar", "Failed to refresh risk repository", e)
            Result.failure(e)
        }
    }
}