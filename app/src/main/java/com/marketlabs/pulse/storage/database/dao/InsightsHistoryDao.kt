package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.InsightsHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightsHistoryDao {

    @Query("SELECT * FROM insights_history WHERE metricId = :metricId")
    fun getHistoryStream(metricId: String): Flow<InsightsHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: InsightsHistoryEntity)
}
