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

    // SSOT (Single Source of Truth): Just points to the local database flow
    override fun getMarketSummaryStream(): Flow<MarketPulse?> {
        return localDataSource.getLatestMarketPulse()
    }

    // SMART REFRESH: Handles Live Updates, HITL Fixes, and Stale Data Checks
    override suspend fun refreshMarketSummary(force: Boolean): Result<Unit> {
        return try {
            // 1. Snapshot current state
            val localData = localDataSource.getLatestMarketPulse().firstOrNull()

            // 2. Calculate Market Time Context
            val todayDateId = getTodayDateString()      // e.g., "2026-02-07"
            val yesterdayDateId = getYesterdayDateString() // e.g., "2026-02-06"
            val midnightToday = getMidnightTimestamp()  // Epoch millis for 00:00 today

            // 3. DECISION MATRIX 🧠
            val shouldFetch = when {
                // Case A: User Pull-to-Refresh (Fixes HITL typos) -> ALWAYS FETCH
                force -> true

                // Case B: Empty DB -> ALWAYS FETCH
                localData == null -> true

                // Case C: Data is from Today -> ALWAYS FETCH
                // (We want hourly updates as the market moves)
                localData.dateId == todayDateId -> true

                // Case D: Data is from Yesterday (The "Midnight Rule")
                // Check: Did we fetch the final wrap-up report *after* the day ended?
                localData.dateId == yesterdayDateId -> {
                    // If last sync was BEFORE midnight, we might have missed the later updates.
                    // If last sync was AFTER midnight, we have the final version.
                    localData.lastSyncedTimestamp?.let { it < midnightToday } ?: true
                }

                // Case E: Old History -> SKIP
                // (Assuming 2+ day old reports are finalized and won't change)
                else -> false
            }

            if (!shouldFetch) {
                return Result.success(Unit) // Skip network, return success
            }

            // 4. Perform Fetch & Save
            remoteDataSource.getLatestMarketPulse().map { freshPulse ->
                // The save operation converts Domain -> Entity,
                // setting 'lastSyncedTimestamp' to NOW automatically.
                localDataSource.saveMarketPulse(freshPulse)

                // Return Unit to indicate success
                Unit
            }

        } catch (e: Exception) {
            // 5. Handle Network/Parsing Failures
            Log.e("MarketPulse", "Failed to fetch", e)
            Result.failure(e)
        }
    }
}