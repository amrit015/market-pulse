package com.marketlabs.pulse.storage.model.dashboard.mappers

import com.marketlabs.pulse.network.model.weeklyPlaybook.NetworkWeeklyEvent
import com.marketlabs.pulse.network.model.weeklyPlaybook.NetworkWeeklyPlaybook
import com.marketlabs.pulse.storage.database.entity.WeeklyPlaybookEntity
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyEvent
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook

// Network -> Domain
fun NetworkWeeklyPlaybook.toDomain(): WeeklyPlaybook {
    return WeeklyPlaybook(
        lastUpdated = this.lastUpdated,
        lastSyncedTimestamp = System.currentTimeMillis(),
        weekStarting = this.weekStarting,
        events = this.events?.map { it.toDomain() }
    )
}

private fun NetworkWeeklyEvent.toDomain(): WeeklyEvent {
    return WeeklyEvent(
        eventName = this.eventName,
        date = this.date,
        actual = this.actual,
        estimate = this.estimate,
        previous = this.previous,
        marketContext = this.marketContext,
        postReleaseImpact = this.postReleaseImpact
    )
}

// Domain -> Entity
fun WeeklyPlaybook.toEntity(): WeeklyPlaybookEntity {
    return WeeklyPlaybookEntity(
        id = "latest",
        lastSyncedTimestamp = System.currentTimeMillis(),
        lastUpdated = this.lastUpdated,
        weekStarting = this.weekStarting,
        events = this.events
    )
}

// Entity -> Domain
fun WeeklyPlaybookEntity.toDomain(): WeeklyPlaybook {
    return WeeklyPlaybook(
        lastUpdated = this.lastUpdated,
        lastSyncedTimestamp = System.currentTimeMillis(),
        weekStarting = this.weekStarting,
        events = this.events
    )
}