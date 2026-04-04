package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyEvent

@Entity(tableName = "weekly_playbook")
data class WeeklyPlaybookEntity(
    @PrimaryKey val id: String = "latest",
    val lastUpdated: Long? = null,
    val lastSyncedTimestamp: Long? = null,
    val weekStarting: String? = null,
    val events: List<WeeklyEvent>? = null
)