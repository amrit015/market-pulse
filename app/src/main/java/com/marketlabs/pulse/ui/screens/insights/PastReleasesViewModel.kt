package com.marketlabs.pulse.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.pastReleases.PastReleasesPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Only the persisted expand/collapse default for the Past Releases list. The releases themselves
 * come from `InsightsViewModel`, which already holds the cached stream this page reads.
 */
@HiltViewModel
class PastReleasesViewModel @Inject constructor(
    private val preferences: PastReleasesPreferencesRepository
) : ViewModel() {

    val expandImpactByDefault: StateFlow<Boolean> = preferences.expandImpactByDefault
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setExpandImpactByDefault(expanded: Boolean) {
        viewModelScope.launch { preferences.setExpandImpactByDefault(expanded) }
    }
}
