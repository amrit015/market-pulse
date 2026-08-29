package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.ChartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChartsDao {

    @Query("SELECT * FROM market_charts WHERE symbol = :symbol AND rangeKey = :rangeKey")
    fun getChartStream(symbol: String, rangeKey: String): Flow<ChartEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChart(chart: ChartEntity)
}
