package com.marketlabs.pulse.data.privacy

import android.content.Context
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AnalyticsConsentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AnalyticsConsentRepository {

    override val enabled: Flow<Boolean> = context.analyticsConsentDataStore.data.map { prefs ->
        prefs[AnalyticsConsentPreferences.ANALYTICS_ENABLED] ?: true
    }

    override suspend fun setEnabled(enabled: Boolean) {
        context.analyticsConsentDataStore.edit { prefs ->
            prefs[AnalyticsConsentPreferences.ANALYTICS_ENABLED] = enabled
        }
    }
}
