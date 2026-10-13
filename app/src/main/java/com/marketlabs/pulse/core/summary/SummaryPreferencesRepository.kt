package com.marketlabs.pulse.core.summary

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Local-only Summary screen preferences. */
interface SummaryPreferencesRepository {

    /**
     * Whether Market Movers and Macro Mix rows start expanded. Defaults to false (collapsed) until
     * the user changes it, and is remembered across sessions.
     */
    val expandItemsByDefault: Flow<Boolean>

    suspend fun setExpandItemsByDefault(expanded: Boolean)
}

private val Context.summaryDataStore by preferencesDataStore(name = "summary_preferences")

private val EXPAND_ITEMS_BY_DEFAULT = booleanPreferencesKey("expand_items_by_default")

@Singleton
class SummaryPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SummaryPreferencesRepository {

    override val expandItemsByDefault: Flow<Boolean> = context.summaryDataStore.data.map { prefs ->
        prefs[EXPAND_ITEMS_BY_DEFAULT] ?: false
    }

    override suspend fun setExpandItemsByDefault(expanded: Boolean) {
        context.summaryDataStore.edit { prefs -> prefs[EXPAND_ITEMS_BY_DEFAULT] = expanded }
    }
}
