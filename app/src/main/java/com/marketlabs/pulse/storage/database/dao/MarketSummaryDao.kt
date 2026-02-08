package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.MarketSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketSummaryDao {

    // Get the SINGLE latest report
    @Query("SELECT * FROM market_pulse ORDER BY serverTimestamp DESC LIMIT 1")
    fun getLatestPulse(): Flow<MarketSummaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPulse(pulse: MarketSummaryEntity)

    @Query("DELETE FROM market_pulse")
    suspend fun clearAll()

}