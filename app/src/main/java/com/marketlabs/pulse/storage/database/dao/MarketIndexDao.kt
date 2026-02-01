package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.MarketIndexEntity
import com.marketlabs.pulse.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketIndexDao {
    // Repository calls this to get data
    @Query("SELECT * FROM ${Constants.MARKET_INDEX_TABLE}")
    fun getAllMarketIndices(): Flow<List<MarketIndexEntity>>

    // Repository calls this to update the cache
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertMarketIndices(stocks: List<MarketIndexEntity>)
}