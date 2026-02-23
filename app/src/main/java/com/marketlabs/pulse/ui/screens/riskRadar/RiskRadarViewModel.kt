package com.marketlabs.pulse.ui.screens.riskRadar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.riskRadar.RiskRadarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RiskRadarViewModel @Inject constructor(
    private val repository: RiskRadarRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // Combine our local loading/error states with the persistent Room stream
    val uiState: StateFlow<RiskRadarUiState> = combine(
        repository.getRiskStream(),
        _isLoading,
        _errorMessage
    ) { riskData, loading, error ->
        RiskRadarUiState(
            isLoading = loading,
            riskRadar = riskData,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RiskRadarUiState(isLoading = true)
    )

    init {
        // Initial fetch (will use cache if valid)
        refreshRisk(force = false)
    }

    fun refreshRisk(force: Boolean = true) {
        viewModelScope.launch {
            _isLoading.update { true }
            _errorMessage.update { null }

            repository.refreshRisk(force)
                .onFailure { error ->
                    _errorMessage.update { error.localizedMessage ?: "Failed to fetch Risk Radar" }
                }

            _isLoading.update { false }
        }
    }
}