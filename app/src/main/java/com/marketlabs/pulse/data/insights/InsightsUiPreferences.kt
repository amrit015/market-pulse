package com.marketlabs.pulse.data.insights

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore key definitions for the Positioning/Posture first-time explainer's "seen it, don't
 * show again" flags (2026-08-27 interpretive-layer spec). Two keys, one per screen -- a user may
 * have already seen the pre-revamp Posture screen and now sees new fields, so its dismissal is
 * tracked independently from Positioning's, not as one combined "seen insights" flag. Its own
 * DataStore file (not reusing `theme_preferences`, matching `ThemeRepository`'s own doc comment
 * that a lighter, Room-less repository is precedented for a domain this small) rather than folding
 * unrelated one-time-UI state into a file literally named for theme preference.
 */
object InsightsUiPreferences {
    val POSITIONING_INTRO_DISMISSED: Preferences.Key<Boolean> = booleanPreferencesKey("positioning_intro_dismissed")
    val POSTURE_INTRO_DISMISSED: Preferences.Key<Boolean> = booleanPreferencesKey("posture_intro_dismissed")
}

val Context.insightsUiDataStore by preferencesDataStore(name = "insights_ui_preferences")
