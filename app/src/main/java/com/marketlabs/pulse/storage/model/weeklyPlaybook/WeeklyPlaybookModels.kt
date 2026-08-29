package com.marketlabs.pulse.storage.model.weeklyPlaybook

/**
 * Represents the AI-generated Weekly Event Playbook.
 */
// 💡 2026-08-29 revision: added `synthesis` (Gemini narrative layer, same shape Posture/
// Positioning/Risks already carry). Per-event `actual`/`postReleaseImpact` handling is unchanged.
data class WeeklyPlaybook(
    val lastUpdated: Long? = null,
    val lastSyncedTimestamp: Long? = null,
    val weekStarting: String? = null,
    val events: List<WeeklyEvent>? = null,
    val synthesis: PlaybookSynthesis? = null
)

/**
 * Represents a single high-impact economic event and its market context.
 */
data class WeeklyEvent(
    val eventName: String? = null,
    val date: String? = null,
    val actual: String? = null,
    val estimate: String? = null,
    val previous: String? = null,
    val marketContext: String? = null,
    val postReleaseImpact: String? = null
)

// 💡 Named PlaybookSynthesis, matching this domain's existing naming (WeeklyPlaybook/WeeklyEvent)
// rather than Posture/Positioning's "Domain"-prefixed naming -- same field shape as
// DomainPostureSynthesis/DomainPositioningSynthesis/MarketRiskSynthesis, kept as its own duplicated
// class per this app's per-domain vertical-slicing convention (see NetworkSynthesis's own doc
// comment, network/model/weeklyPlaybook/RemoteWeeklyPlaybook.kt).
data class PlaybookSynthesis(
    val headline: String?,
    val detail: String?,
    val generatedAt: Long?,
    val contentFlags: List<String>,
    val state: String?
)