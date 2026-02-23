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
    @Query("SELECT * FROM market_pulse ORDER BY serverTimestamp DESC LIMIT 1")
    fun getLatestMarketPulse(): Flow<MarketPulseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPulse(pulse: MarketPulseEntity)

    @Query("DELETE FROM market_pulse")
    suspend fun clearMarketPulse()


    // --- V2.5 (Gemini 2.5 Pro - Banner Content) ---
    @Query("SELECT * FROM daily_pulse ORDER BY serverTimestamp DESC LIMIT 1")
    fun getLatestDailyPulse(): Flow<DailyPulseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyPulse(pulse: DailyPulseEntity)

    @Query("DELETE FROM daily_pulse")
    suspend fun clearDailyPulse()
}