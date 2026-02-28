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
}