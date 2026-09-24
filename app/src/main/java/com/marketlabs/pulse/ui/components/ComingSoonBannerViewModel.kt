package com.marketlabs.pulse.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.data.stockAnalysis.StockAnalysisUiStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the custom-list announcement banner (Analysis screen and Settings' "Coming Up" page).
 * Dismissal is permanent -- there's no toggle to re-enable, unlike `NotificationEnablePromptViewModel`'s
 * preferences, so once answered it never reappears anywhere it's used.
 */
@HiltViewModel
class ComingSoonBannerViewModel @Inject constructor(
    private val repository: StockAnalysisUiStateRepository
) : ViewModel() {

    val isDismissed: StateFlow<Boolean> = repository.isCustomListBannerDismissed.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        // 💡 Starts `false`, matching the common case (never dismissed) -- the Analysis screen picks
        // between this banner and `NotificationEnableBanner` based on this value (see
        // StockAnalysisRoute.kt), so defaulting to the less common "already dismissed" state would
        // show the wrong one first and swap a frame later once the real stored value loads.
        initialValue = false
    )

    fun dismiss() {
        viewModelScope.launch { repository.dismissCustomListBanner() }
    }
}
