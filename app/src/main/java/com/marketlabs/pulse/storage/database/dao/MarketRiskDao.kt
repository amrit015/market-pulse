package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.MarketRiskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketRiskDao {

    @Query("SELECT * FROM market_tail_risks WHERE date = :dateString")
    fun getTailRisksByDate(dateString: String): Flow<MarketRiskEntity?>

    @Query("SELECT * FROM market_tail_risks ORDER BY date DESC LIMIT 1")
    fun getLatestCachedTailRisks(): Flow<MarketRiskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTailRisks(risks: MarketRiskEntity)

    // ========================================================================
    // SYNC MANAGER TIMESTAMP QUERIES
    // ========================================================================

    @Query("SELECT lastSyncedTimestamp FROM market_tail_risks ORDER BY date DESC LIMIT 1")
    suspend fun getLastSyncedTimestampTailRisks(): Long?

    @Query("UPDATE market_tail_risks SET lastSyncedTimestamp = :timestamp WHERE date = (SELECT date FROM market_tail_risks ORDER BY date DESC LIMIT 1)")
    suspend fun updateLastSyncedTimestampTailRisks(timestamp: Long)
}