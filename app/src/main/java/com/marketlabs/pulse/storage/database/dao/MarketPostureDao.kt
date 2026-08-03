package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.MarketPostureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketPostureDao {
    @Query("SELECT * FROM market_posture WHERE id = 'market_posture_id'")
    fun getMarketPostureStream(): Flow<MarketPostureEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPosture(entity: MarketPostureEntity)

    @Query("DELETE FROM market_posture")
    suspend fun clearMarketPosture()

    // 💡 NEW: Timestamp tracking methods for the SyncManager
    @Query("SELECT timestamp FROM market_posture WHERE id = 'market_posture_id'")
    suspend fun getLastSyncedTimestamp(): Long?

    @Query("UPDATE market_posture SET timestamp = :timestamp WHERE id = 'market_posture_id'")
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}