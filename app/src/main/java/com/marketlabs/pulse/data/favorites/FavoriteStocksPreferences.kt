package com.marketlabs.pulse.data.favorites

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore key definitions for the Analysis tab's favorited symbols. This is purely a local,
 * per-device preference -- not synced with the backend or any other client -- so it lives in its
 * own DataStore file rather than piggybacking on `theme_preferences`, matching the same
 * one-domain-per-file split `InsightsUiPreferences` already established.
 */
object FavoriteStocksPreferences {
    val FAVORITE_SYMBOLS: Preferences.Key<Set<String>> = stringSetPreferencesKey("favorite_symbols")
}

val Context.favoriteStocksDataStore by preferencesDataStore(name = "favorite_stocks_preferences")
