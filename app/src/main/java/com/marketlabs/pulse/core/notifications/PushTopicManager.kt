package com.marketlabs.pulse.core.notifications

import com.marketlabs.pulse.data.notifications.NotificationPreference

/**
 * Keeps FCM topic subscriptions in line with the persisted notification preferences. Subscribing
 * doesn't need the POST_NOTIFICATIONS permission — only displaying the notification does — so
 * nothing here looks at it.
 */
interface PushTopicManager {

    /** Subscribes to (or unsubscribes from) every topic mapped to [preference]. */
    fun apply(preference: NotificationPreference, enabled: Boolean)

    /**
     * Re-issues subscribe/unsubscribe for every preference from its persisted value. Both calls are
     * idempotent, so running this on each app start heals calls that failed (offline toggle),
     * reinstalls, and a cleared FCM registration.
     */
    suspend fun reconcile()
}
