package com.marketlabs.pulse.data.notifications

/** The user-facing push toggles. Each one maps to one or more FCM topics (see `NotificationTopics`). */
enum class NotificationPreference {
    DAILY_SUMMARY,
    STOCK_ANALYSIS
}
