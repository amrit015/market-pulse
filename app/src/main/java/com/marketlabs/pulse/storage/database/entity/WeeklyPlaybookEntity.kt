package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyEvent

// 💡 The `synthesis` block is stored as flat nullable columns (synthesisHeadline,
// synthesisDetail, ...), matching MarketPostureEntity's convention for the same narrative layer.
@Entity(tableName = "weekly_playbook")
data class WeeklyPlaybookEntity(
    @PrimaryKey val id: String = "latest",
    val lastUpdated: Long? = null,
    val lastSyncedTimestamp: Long? = null,
    val weekStarting: String? = null,
    val events: List<WeeklyEvent>? = null,

    // Synthesis (Gemini narrative layer)
    val synthesisHeadline: String? = null,
    val synthesisDetail: String? = null,
    val synthesisGeneratedAt: Long? = null,
    val synthesisContentFlags: List<String>? = null,
    val synthesisState: String? = null
)