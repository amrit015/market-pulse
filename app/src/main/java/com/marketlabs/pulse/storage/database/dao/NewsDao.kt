package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.MarketNewsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM market_news WHERE id = 'latest'")
    fun getLatestNews(): Flow<MarketNewsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: MarketNewsEntity)

    @Query("DELETE FROM market_news")
    suspend fun clearNews()
}