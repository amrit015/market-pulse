package com.marketlabs.pulse.data.favorites

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore key definitions for the Indicators tab's favorited metric ids. Local, per-device
 * preference, same shape `FavoriteStocksPreferences` uses for the Analysis tab's favorited
 * symbols -- its own DataStore file (not reusing `favorite_stocks_preferences`) since a metric id
 * (`"spy_rsi"`, `"pe_ratio"`) and a stock symbol (`"AAPL"`) are different id spaces that happen to
 * both be strings, not one shared favorites concept.
 */
object FavoriteMetricsPreferences {
    val FAVORITE_METRIC_IDS: Preferences.Key<Set<String>> = stringSetPreferencesKey("favorite_metric_ids")
}

val Context.favoriteMetricsDataStore by preferencesDataStore(name = "favorite_metrics_preferences")
