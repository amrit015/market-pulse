package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.MetricHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MetricHistoryDao {

    @Query("SELECT * FROM metric_history WHERE metricId = :metricId")
    fun getHistoryStream(metricId: String): Flow<MetricHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: MetricHistoryEntity)
}
