package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.MarketRiskAssessmentEntity
import com.marketlabs.pulse.storage.database.entity.RiskRadarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RiskRadarDao {
    @Query("SELECT * FROM market_risk WHERE date = :dateString")
    fun getRiskByDate(dateString: String): Flow<RiskRadarEntity?>

    @Query("SELECT * FROM market_risk ORDER BY date DESC LIMIT 1")
    fun getLatestCachedRisk(): Flow<RiskRadarEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRisk(risk: RiskRadarEntity)

    @Query("SELECT * FROM market_tail_risks WHERE date = :dateString")
    fun getTailRisksByDate(dateString: String): Flow<MarketRiskAssessmentEntity?>

    @Query("SELECT * FROM market_tail_risks ORDER BY date DESC LIMIT 1")
    fun getLatestCachedTailRisks(): Flow<MarketRiskAssessmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTailRisks(risks: MarketRiskAssessmentEntity)

    // ========================================================================
    // SYNC MANAGER TIMESTAMP QUERIES
    // ========================================================================

    @Query("SELECT lastSyncedTimestamp FROM market_risk ORDER BY date DESC LIMIT 1")
    suspend fun getLastSyncedTimestampRisk(): Long?

    @Query("UPDATE market_risk SET lastSyncedTimestamp = :timestamp WHERE date = (SELECT date FROM market_risk ORDER BY date DESC LIMIT 1)")
    suspend fun updateLastSyncedTimestampRisk(timestamp: Long)

    @Query("SELECT lastSyncedTimestamp FROM market_tail_risks ORDER BY date DESC LIMIT 1")
    suspend fun getLastSyncedTimestampTailRisks(): Long?

    @Query("UPDATE market_tail_risks SET lastSyncedTimestamp = :timestamp WHERE date = (SELECT date FROM market_tail_risks ORDER BY date DESC LIMIT 1)")
    suspend fun updateLastSyncedTimestampTailRisks(timestamp: Long)
}