package com.marketlabs.pulse.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.notifications.PushTopicManager
import com.marketlabs.pulse.data.notifications.NotificationPreference
import com.marketlabs.pulse.data.notifications.NotificationPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the "turn on notifications" banner. A banner shows only while its preference is off and the
 * user hasn't answered it yet; enabling and dismissing both count as an answer, so it never
 * reappears (even if the toggle is later switched back off in Settings).
 *
 * No `SyncManager`/lifecycle hooks, same exception as `SettingsViewModel`: live DataStore flows.
 */
@HiltViewModel
class NotificationEnablePromptViewModel @Inject constructor(
    private val preferencesRepository: NotificationPreferencesRepository,
    private val pushTopicManager: PushTopicManager
) : ViewModel() {

    /**
     * Preferences whose banner should be visible. Starts empty so a banner never flashes in before
     * the stored answer has loaded.
     */
    val visiblePrompts: StateFlow<Set<NotificationPreference>> = combine(
        preferencesRepository.enabledPreferences,
        preferencesRepository.dismissedPrompts
    ) { enabled, dismissed ->
        NotificationPreference.entries.filter { it !in enabled && it !in dismissed }.toSet()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    fun enable(preference: NotificationPreference) {
        viewModelScope.launch {
            preferencesRepository.setEnabled(preference, true)
            preferencesRepository.dismissPrompt(preference)
            pushTopicManager.apply(preference, true)
        }
    }

    fun dismiss(preference: NotificationPreference) {
        viewModelScope.launch { preferencesRepository.dismissPrompt(preference) }
    }
}
