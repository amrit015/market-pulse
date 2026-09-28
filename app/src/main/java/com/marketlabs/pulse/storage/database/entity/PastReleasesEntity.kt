package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.marketlabs.pulse.storage.model.pastReleases.PastRelease

// 💡 Singleton row, same shape as WeeklyPlaybookEntity -- the backend returns every release's slot
// in one document/call, so there's no need for one row per release id.
@Entity(tableName = "past_releases")
data class PastReleasesEntity(
    @PrimaryKey val id: String = "latest",
    val lastSyncedTimestamp: Long? = null,
    val releases: Map<String, PastRelease>? = null
)
