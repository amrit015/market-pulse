package com.marketlabs.pulse.data.notifications

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/** DataStore key definitions for the local-only push-notification toggles — same shape as `LegalPreferences`. */
object NotificationPreferences {
    val DAILY_SUMMARY_ENABLED: Preferences.Key<Boolean> = booleanPreferencesKey("daily_summary_enabled")
    val STOCK_ANALYSIS_ENABLED: Preferences.Key<Boolean> = booleanPreferencesKey("stock_analysis_enabled")

    val DAILY_SUMMARY_PROMPT_DISMISSED: Preferences.Key<Boolean> = booleanPreferencesKey("daily_summary_prompt_dismissed")
    val STOCK_ANALYSIS_PROMPT_DISMISSED: Preferences.Key<Boolean> = booleanPreferencesKey("stock_analysis_prompt_dismissed")

    fun promptDismissedKeyFor(preference: NotificationPreference): Preferences.Key<Boolean> = when (preference) {
        NotificationPreference.DAILY_SUMMARY -> DAILY_SUMMARY_PROMPT_DISMISSED
        NotificationPreference.STOCK_ANALYSIS -> STOCK_ANALYSIS_PROMPT_DISMISSED
    }

    fun keyFor(preference: NotificationPreference): Preferences.Key<Boolean> = when (preference) {
        NotificationPreference.DAILY_SUMMARY -> DAILY_SUMMARY_ENABLED
        NotificationPreference.STOCK_ANALYSIS -> STOCK_ANALYSIS_ENABLED
    }
}

val Context.notificationDataStore by preferencesDataStore(name = "notification_preferences")
