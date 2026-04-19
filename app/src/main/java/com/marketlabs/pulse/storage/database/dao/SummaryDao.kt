package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.DailyPulseEntity
import com.marketlabs.pulse.storage.database.entity.MarketPulseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {

    // --- V3 (Gemini 3.1 Pro - Main Content) ---
    @Query("SELECT * FROM market_pulse ORDER BY lastUpdated DESC LIMIT 1")
    fun getLatestMarketPulse(): Flow<MarketPulseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPulse(pulse: MarketPulseEntity)

    @Query("DELETE FROM market_pulse")
    suspend fun clearMarketPulse()


    // --- V2.5 (Gemini 2.5 Pro - Banner Content) ---
    @Query("SELECT * FROM daily_pulse ORDER BY lastUpdated DESC LIMIT 1")
    fun getLatestDailyPulse(): Flow<DailyPulseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyPulse(pulse: DailyPulseEntity)

    @Query("DELETE FROM daily_pulse")
    suspend fun clearDailyPulse()

    // ========================================================================
    // SYNC MANAGER TIMESTAMP QUERIES
    // ========================================================================

    /**
     * Fetches only the timestamp to avoid loading the heavy AI JSON into memory.
     */
    @Query("SELECT lastSyncedTimestamp FROM market_pulse ORDER BY lastUpdated DESC LIMIT 1")
    suspend fun getLastSyncedTimestamp(): Long?

    /**
     * Updates only the timestamp without overwriting the existing summary text.
     */
    @Query("UPDATE market_pulse SET lastSyncedTimestamp = :timestamp WHERE dateId = (SELECT dateId FROM market_pulse ORDER BY lastUpdated DESC LIMIT 1)")
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}