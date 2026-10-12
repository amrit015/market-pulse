package com.marketlabs.pulse.core.pastReleases

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local-only Past Releases screen preferences. Deliberately its own setting, independent of the
 * Summary screen's expanded/collapsed default: the two lists are different content and a reader
 * can reasonably want one open and the other closed.
 */
interface PastReleasesPreferencesRepository {

    /**
     * Whether every card's Post-Release Impact starts expanded. Defaults to false (collapsed)
     * until the user changes it, and is remembered across sessions.
     */
    val expandImpactByDefault: Flow<Boolean>

    suspend fun setExpandImpactByDefault(expanded: Boolean)
}

private val Context.pastReleasesDataStore by preferencesDataStore(name = "past_releases_preferences")

private val EXPAND_IMPACT_BY_DEFAULT = booleanPreferencesKey("expand_impact_by_default")

@Singleton
class PastReleasesPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PastReleasesPreferencesRepository {

    override val expandImpactByDefault: Flow<Boolean> = context.pastReleasesDataStore.data.map { prefs ->
        prefs[EXPAND_IMPACT_BY_DEFAULT] ?: false
    }

    override suspend fun setExpandImpactByDefault(expanded: Boolean) {
        context.pastReleasesDataStore.edit { prefs -> prefs[EXPAND_IMPACT_BY_DEFAULT] = expanded }
    }
}
