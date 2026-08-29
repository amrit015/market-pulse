package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.MarketPositioningEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketPositioningDao {
    @Query("SELECT * FROM market_positioning WHERE id = 'market_positioning_id'")
    fun getMarketPositioningStream(): Flow<MarketPositioningEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPositioning(entity: MarketPositioningEntity)

    @Query("DELETE FROM market_positioning")
    suspend fun clearMarketPositioning()

    @Query("SELECT timestamp FROM market_positioning WHERE id = 'market_positioning_id'")
    suspend fun getLastSyncedTimestamp(): Long?

    @Query("UPDATE market_positioning SET timestamp = :timestamp WHERE id = 'market_positioning_id'")
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}
