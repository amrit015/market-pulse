package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.RiskRadarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RiskRadarDao {
    // Gets the specific cache for a given date string
    @Query("SELECT * FROM market_risk WHERE date = :dateString")
    fun getRiskByDate(dateString: String): Flow<RiskRadarEntity?>

    // Safely gets the most recently saved entry (perfect for Weekends!)
    @Query("SELECT * FROM market_risk ORDER BY date DESC LIMIT 1")
    fun getLatestCachedRisk(): Flow<RiskRadarEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRisk(risk: RiskRadarEntity)
}