package com.marketlabs.pulse.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.analytics.AnalyticsCollection
import com.marketlabs.pulse.core.analytics.AnalyticsTracker
import com.marketlabs.pulse.data.privacy.AnalyticsConsentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * No `SyncManager` and no `onStart()`/`onStop()`, same exception as `SettingsViewModel`: the
 * switch is a live DataStore `Flow`, not sync-flag-driven remote data.
 */
@HiltViewModel
class DataSyncViewModel @Inject constructor(
    private val consentRepository: AnalyticsConsentRepository,
    private val tracker: AnalyticsTracker
) : ViewModel() {

    val uiState: StateFlow<DataSyncUiState> = consentRepository.enabled
        .map { DataSyncUiState(analyticsEnabled = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DataSyncUiState()
        )

    /**
     * Persists the choice and applies it immediately rather than waiting for the app-level
     * collector, so events stop (or resume) the moment the switch flips. No event is logged for
     * the change itself.
     */
    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            consentRepository.setEnabled(enabled)
            tracker.setCollectionEnabled(AnalyticsCollection.effective(enabled))
        }
    }
}
