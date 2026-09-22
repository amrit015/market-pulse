package com.marketlabs.pulse.data.notifications

import android.content.Context
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationPreferencesRepository {

    override val enabledPreferences: Flow<Set<NotificationPreference>> =
        context.notificationDataStore.data.map { prefs ->
            NotificationPreference.entries
                .filter { prefs[NotificationPreferences.keyFor(it)] ?: false }
                .toSet()
        }

    override val dismissedPrompts: Flow<Set<NotificationPreference>> =
        context.notificationDataStore.data.map { prefs ->
            NotificationPreference.entries
                .filter { prefs[NotificationPreferences.promptDismissedKeyFor(it)] ?: false }
                .toSet()
        }

    override suspend fun dismissPrompt(preference: NotificationPreference) {
        context.notificationDataStore.edit { prefs ->
            prefs[NotificationPreferences.promptDismissedKeyFor(preference)] = true
        }
    }

    override suspend fun setEnabled(preference: NotificationPreference, enabled: Boolean) {
        context.notificationDataStore.edit { prefs ->
            prefs[NotificationPreferences.keyFor(preference)] = enabled
        }
    }
}
