package com.marketlabs.pulse.data.favorites

import android.content.Context
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteStocksRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FavoriteStocksRepository {

    override val favoriteSymbols: Flow<Set<String>> = context.favoriteStocksDataStore.data.map { prefs ->
        prefs[FavoriteStocksPreferences.FAVORITE_SYMBOLS] ?: emptySet()
    }

    override val clickedDeepDiveSymbols: Flow<Set<String>> = context.favoriteStocksDataStore.data.map { prefs ->
        prefs[FavoriteStocksPreferences.CLICKED_DEEP_DIVE_SYMBOLS] ?: emptySet()
    }

    override suspend fun toggleFavorite(symbol: String) {
        context.favoriteStocksDataStore.edit { prefs ->
            val current = prefs[FavoriteStocksPreferences.FAVORITE_SYMBOLS] ?: emptySet()
            prefs[FavoriteStocksPreferences.FAVORITE_SYMBOLS] = if (symbol in current) current - symbol else current + symbol
        }
    }

    override suspend fun markDeepDiveClicked(symbol: String) {
        context.favoriteStocksDataStore.edit { prefs ->
            val current = prefs[FavoriteStocksPreferences.CLICKED_DEEP_DIVE_SYMBOLS] ?: emptySet()
            if (symbol !in current) {
                prefs[FavoriteStocksPreferences.CLICKED_DEEP_DIVE_SYMBOLS] = current + symbol
            }
        }
    }
}
