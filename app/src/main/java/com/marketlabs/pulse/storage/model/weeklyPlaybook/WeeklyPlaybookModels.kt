package com.marketlabs.pulse.storage.model.weeklyPlaybook

/**
 * Represents the AI-generated Weekly Event Playbook.
 */
data class WeeklyPlaybook(
    val lastUpdated: Long? = null,
    val lastSyncedTimestamp: Long? = null,
    val weekStarting: String? = null,
    val events: List<WeeklyEvent>? = null
)

/**
 * Represents a single high-impact economic event and its market context.
 */
data class WeeklyEvent(
    val eventName: String? = null,
    val date: String? = null,
    val estimate: String? = null,
    val previous: String? = null,
    val marketContext: String? = null
)