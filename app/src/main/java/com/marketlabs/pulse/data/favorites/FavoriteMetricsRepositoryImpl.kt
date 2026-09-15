package com.marketlabs.pulse.data.favorites

import android.content.Context
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteMetricsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FavoriteMetricsRepository {

    override val favoriteMetricIds: Flow<Set<String>> = context.favoriteMetricsDataStore.data.map { prefs ->
        prefs[FavoriteMetricsPreferences.FAVORITE_METRIC_IDS] ?: emptySet()
    }

    override suspend fun toggleFavorite(metricId: String) {
        context.favoriteMetricsDataStore.edit { prefs ->
            val current = prefs[FavoriteMetricsPreferences.FAVORITE_METRIC_IDS] ?: emptySet()
            prefs[FavoriteMetricsPreferences.FAVORITE_METRIC_IDS] = if (metricId in current) current - metricId else current + metricId
        }
    }
}
