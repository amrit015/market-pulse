package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.IndicatorsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IndicatorsDao {
    @Query("SELECT * FROM market_indicators WHERE dateId = :dateString")
    fun getIndicatorsByDate(dateString: String): Flow<IndicatorsEntity?>

    @Query("SELECT * FROM market_indicators ORDER BY dateId DESC LIMIT 1")
    fun getLatestCachedIndicators(): Flow<IndicatorsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIndicators(indicators: IndicatorsEntity)

    /**
     * Retrieves the last synced timestamp from the most recent indicators record.
     */
    @Query("SELECT lastSyncedTimestamp FROM market_indicators ORDER BY dateId DESC LIMIT 1")
    suspend fun getLastSyncedTimestamp(): Long?

    /**
     * Updates the last synced timestamp for the most recent indicators record.
     * This avoids needing to overwrite the heavy JSON pillars just to update the time.
     */
    @Query("UPDATE market_indicators SET lastSyncedTimestamp = :timestamp WHERE dateId = (SELECT dateId FROM market_indicators ORDER BY dateId DESC LIMIT 1)")
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}