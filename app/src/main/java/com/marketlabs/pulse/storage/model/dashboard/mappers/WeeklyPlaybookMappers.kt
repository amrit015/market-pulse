package com.marketlabs.pulse.storage.model.dashboard.mappers

import com.marketlabs.pulse.network.model.weeklyPlaybook.NetworkSynthesis
import com.marketlabs.pulse.network.model.weeklyPlaybook.NetworkWeeklyEvent
import com.marketlabs.pulse.network.model.weeklyPlaybook.NetworkWeeklyPlaybook
import com.marketlabs.pulse.storage.database.entity.WeeklyPlaybookEntity
import com.marketlabs.pulse.storage.model.weeklyPlaybook.PlaybookSynthesis
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyEvent
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook

// Network -> Domain
fun NetworkWeeklyPlaybook.toDomain(): WeeklyPlaybook {
    return WeeklyPlaybook(
        lastUpdated = this.lastUpdated,
        lastSyncedTimestamp = System.currentTimeMillis(),
        weekStarting = this.weekStarting,
        events = this.events?.map { it.toDomain() },
        synthesis = this.synthesis?.toDomain()
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

private fun NetworkSynthesis.toDomain(): PlaybookSynthesis {
    return PlaybookSynthesis(
        headline = headline,
        detail = detail,
        generatedAt = generatedAt,
        contentFlags = contentFlags ?: emptyList(),
        state = state
    )
}

// Domain -> Entity
fun WeeklyPlaybook.toEntity(): WeeklyPlaybookEntity {
    return WeeklyPlaybookEntity(
        id = "latest",
        lastSyncedTimestamp = System.currentTimeMillis(),
        lastUpdated = this.lastUpdated,
        weekStarting = this.weekStarting,
        events = this.events,
        synthesisHeadline = this.synthesis?.headline,
        synthesisDetail = this.synthesis?.detail,
        synthesisGeneratedAt = this.synthesis?.generatedAt,
        synthesisContentFlags = this.synthesis?.contentFlags,
        synthesisState = this.synthesis?.state
    )
}

// Entity -> Domain
fun WeeklyPlaybookEntity.toDomain(): WeeklyPlaybook {
    return WeeklyPlaybook(
        lastUpdated = this.lastUpdated,
        lastSyncedTimestamp = System.currentTimeMillis(),
        weekStarting = this.weekStarting,
        events = this.events,
        // 💡 Keyed off `state`, not `headline` -- matches MarketPostureEntity.toDomain()'s
        // identical reconstruction, see that mapper's own doc comment for why (the "unavailable"
        // first-run case has a real synthesis object with a null headline/detail but a non-null
        // `state`).
        synthesis = this.synthesisState?.let {
            PlaybookSynthesis(
                headline = this.synthesisHeadline,
                detail = this.synthesisDetail,
                generatedAt = this.synthesisGeneratedAt,
                contentFlags = this.synthesisContentFlags ?: emptyList(),
                state = this.synthesisState
            )
        }
    )
}