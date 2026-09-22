package com.marketlabs.pulse.core.notifications

import com.marketlabs.pulse.BuildConfig
import com.marketlabs.pulse.data.notifications.NotificationPreference

/**
 * The single place FCM topic names live. Release builds subscribe to the production topics only;
 * debug builds also subscribe to a `_test` twin of each, which the backend publishes to only while
 * it is deployed with a topic suffix — that lets a debug build receive test sends without any
 * production traffic being aimed at it.
 */
object NotificationTopics {

    private val productionTopics = mapOf(
        NotificationPreference.DAILY_SUMMARY to listOf("daily_summary"),
        NotificationPreference.STOCK_ANALYSIS to listOf("stock_analysis")
    )

    private val testTopics = mapOf(
        NotificationPreference.DAILY_SUMMARY to listOf("daily_summary_test"),
        NotificationPreference.STOCK_ANALYSIS to listOf("stock_analysis_test")
    )

    /** Every topic that must be subscribed while [preference] is on, and unsubscribed when it is off. */
    fun topicsFor(
        preference: NotificationPreference,
        includeTestTopics: Boolean = BuildConfig.DEBUG
    ): List<String> {
        val production = productionTopics.getValue(preference)
        return if (includeTestTopics) production + testTopics.getValue(preference) else production
    }
}
