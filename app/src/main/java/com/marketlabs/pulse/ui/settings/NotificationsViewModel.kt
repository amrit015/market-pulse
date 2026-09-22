package com.marketlabs.pulse.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.notifications.PushTopicManager
import com.marketlabs.pulse.data.notifications.NotificationPreference
import com.marketlabs.pulse.data.notifications.NotificationPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * No `SyncManager` and no `onStart()`/`onStop()`, same exception as `SettingsViewModel`: the
 * preferences are a live DataStore `Flow`, not sync-flag-driven remote data.
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val preferencesRepository: NotificationPreferencesRepository,
    private val pushTopicManager: PushTopicManager
) : ViewModel() {

    val uiState: StateFlow<NotificationsUiState> = preferencesRepository.enabledPreferences
        .map { enabled ->
            NotificationsUiState(
                dailySummaryEnabled = NotificationPreference.DAILY_SUMMARY in enabled,
                stockAnalysisEnabled = NotificationPreference.STOCK_ANALYSIS in enabled
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationsUiState()
        )

    /** Persists the choice, then subscribes/unsubscribes the matching topics. */
    fun setEnabled(preference: NotificationPreference, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setEnabled(preference, enabled)
            pushTopicManager.apply(preference, enabled)
        }
    }
}
