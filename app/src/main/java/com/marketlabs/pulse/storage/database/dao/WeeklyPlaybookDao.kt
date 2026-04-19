package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.WeeklyPlaybookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyPlaybookDao {
    @Query("SELECT * FROM weekly_playbook WHERE id = 'latest'")
    fun getLatestPlaybook(): Flow<WeeklyPlaybookEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaybook(playbook: WeeklyPlaybookEntity)
}