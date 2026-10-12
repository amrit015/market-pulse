package com.marketlabs.pulse.ui.screens.legal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.analytics.AnalyticsEvent
import com.marketlabs.pulse.core.analytics.AnalyticsTracker
import com.marketlabs.pulse.data.legal.LegalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LegalAcceptanceViewModel @Inject constructor(
    private val legalRepository: LegalRepository,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    /** Fire-and-forget, same shape as `SettingsViewModel.selectTheme` — the caller navigates
     * forward immediately rather than waiting on the write. */
    fun acceptCurrentVersion() {
        viewModelScope.launch {
            legalRepository.acceptCurrentVersion()
            analyticsTracker.log(AnalyticsEvent.LegalAccept(LegalRepository.CURRENT_LEGAL_VERSION.toLong()))
        }
    }
}
