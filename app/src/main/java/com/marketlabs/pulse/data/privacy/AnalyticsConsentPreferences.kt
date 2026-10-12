package com.marketlabs.pulse.data.privacy

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/** DataStore key for the "Share usage data" choice — same shape as `LegalPreferences`. */
object AnalyticsConsentPreferences {
    val ANALYTICS_ENABLED: Preferences.Key<Boolean> = booleanPreferencesKey("analytics_enabled")
}

val Context.analyticsConsentDataStore by preferencesDataStore(name = "analytics_consent_preferences")
