package com.marketlabs.pulse.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Exposes the anonymous App ID for the About screen's copyable row; null until sign-in lands. */
@HiltViewModel
class AboutViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    val appId: StateFlow<String?> = authRepository.uid
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), authRepository.currentUid())
}
