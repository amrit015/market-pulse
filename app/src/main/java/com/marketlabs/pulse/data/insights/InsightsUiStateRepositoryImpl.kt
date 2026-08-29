package com.marketlabs.pulse.data.insights

import android.content.Context
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InsightsUiStateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : InsightsUiStateRepository {

    override val isPositioningIntroDismissed: Flow<Boolean> = context.insightsUiDataStore.data.map { prefs ->
        prefs[InsightsUiPreferences.POSITIONING_INTRO_DISMISSED] ?: false
    }

    override val isPostureIntroDismissed: Flow<Boolean> = context.insightsUiDataStore.data.map { prefs ->
        prefs[InsightsUiPreferences.POSTURE_INTRO_DISMISSED] ?: false
    }

    override suspend fun dismissPositioningIntro() {
        context.insightsUiDataStore.edit { prefs ->
            prefs[InsightsUiPreferences.POSITIONING_INTRO_DISMISSED] = true
        }
    }

    override suspend fun dismissPostureIntro() {
        context.insightsUiDataStore.edit { prefs ->
            prefs[InsightsUiPreferences.POSTURE_INTRO_DISMISSED] = true
        }
    }
}
