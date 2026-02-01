package com.marketlabs.pulse.storage.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.Constants.MARKET_INDEX_TABLE
import com.marketlabs.pulse.storage.MarketIndexEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketIndexDao {
    // Repository calls this to get data
    @Query("SELECT * FROM $MARKET_INDEX_TABLE")
    fun getAllMarketIndices(): Flow<List<MarketIndexEntity>>

    // Repository calls this to update the cache
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketIndices(stocks: List<MarketIndexEntity>)
}