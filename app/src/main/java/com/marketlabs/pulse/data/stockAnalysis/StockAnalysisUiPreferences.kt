package com.marketlabs.pulse.data.stockAnalysis

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore key for the custom-list announcement banner's "seen it, don't show again" flag -- same
 * lightweight, Room-less shape `InsightsUiPreferences` established for the Positioning/Posture
 * intro cards. Its own DataStore file rather than folding into that one, since this isn't Insights
 * state.
 */
object StockAnalysisUiPreferences {
    val CUSTOM_LIST_BANNER_DISMISSED: Preferences.Key<Boolean> = booleanPreferencesKey("custom_list_banner_dismissed")
}

val Context.stockAnalysisUiDataStore by preferencesDataStore(name = "stock_analysis_ui_preferences")
