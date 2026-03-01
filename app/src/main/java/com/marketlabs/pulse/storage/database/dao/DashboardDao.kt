package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.marketlabs.pulse.storage.database.entity.AssetOverviewEntity
import com.marketlabs.pulse.storage.database.entity.MarketStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    @Query("SELECT * FROM market_state WHERE id = 1")
    fun getMarketState(): Flow<MarketStateEntity?>

    @Query("SELECT * FROM dashboard_assets ORDER BY symbol ASC")
    fun getDashboardAssets(): Flow<List<AssetOverviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketState(state: MarketStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetOverviewEntity>)

    @Transaction
    suspend fun updateDashboardData(state: MarketStateEntity, assets: List<AssetOverviewEntity>) {
        insertMarketState(state)
        insertAssets(assets)
    }
}