package com.marketlabs.pulse.data.notifications

import kotlinx.coroutines.flow.Flow

/**
 * Thin wrapper over a local DataStore, same reasoning as `LegalRepository`: no Remote/Local split
 * and no Room caching. Only records what the user chose; subscribing to the matching FCM topics is
 * `PushTopicManager`'s job.
 */
interface NotificationPreferencesRepository {

    /** The preferences currently switched on. Both are off until the user opts in. */
    val enabledPreferences: Flow<Set<NotificationPreference>>

    suspend fun setEnabled(preference: NotificationPreference, enabled: Boolean)

    /**
     * The preferences whose in-screen "enable notifications" banner the user has already answered
     * (by enabling or dismissing it), so it stays gone even if the toggle is later turned off.
     */
    val dismissedPrompts: Flow<Set<NotificationPreference>>

    suspend fun dismissPrompt(preference: NotificationPreference)
}
