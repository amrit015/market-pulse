package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.PastReleasesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PastReleasesDao {
    @Query("SELECT * FROM past_releases WHERE id = 'latest'")
    fun getLatestPastReleases(): Flow<PastReleasesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPastReleases(pastReleases: PastReleasesEntity)

    /**
     * Fetches only the timestamp to avoid loading the entire releases map into memory.
     */
    @Query("SELECT lastSyncedTimestamp FROM past_releases WHERE id = 'latest'")
    suspend fun getLastSyncedTimestamp(): Long?

    /**
     * Updates only the timestamp without overwriting the existing releases map.
     */
    @Query("UPDATE past_releases SET lastSyncedTimestamp = :timestamp WHERE id = 'latest'")
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}
