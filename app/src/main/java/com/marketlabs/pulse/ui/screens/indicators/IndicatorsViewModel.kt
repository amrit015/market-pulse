package com.marketlabs.pulse.ui.screens.indicators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.indicators.IndicatorsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IndicatorsViewModel @Inject constructor(
    private val repository: IndicatorsRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // Combine the Room Database Flow with our loading/error states
    val uiState: StateFlow<IndicatorsUiState> = combine(
        repository.getIndicatorsStream(),
        _isLoading,
        _isRefreshing,
        _errorMessage
    ) { indicatorsData, loading, refreshing, error ->
        IndicatorsUiState(
            data = indicatorsData,
            isLoading = loading && indicatorsData == null, // Only show main loader if we have NO data
            isRefreshing = refreshing, // Used for the Swipe-to-Refresh spinner
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IndicatorsUiState(isLoading = true)
    )

    init {
        // Automatically check if we need to fetch new data when the ViewModel spins up
        fetchIndicators(force = false)
    }

    /**
     * Called by Swipe-To-Refresh on the UI
     */
    fun refreshIndicators() {
        fetchIndicators(force = true)
    }

    /**
     * Clears any active error messages (e.g., after a Snackbar is dismissed)
     */
    fun clearError() {
        _errorMessage.value = null
    }

    private fun fetchIndicators(force: Boolean) {
        viewModelScope.launch {
            if (force) {
                _isRefreshing.value = true
            } else {
                _isLoading.value = true
            }
            _errorMessage.value = null

            val result = repository.refreshIndicators(force)

            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to load Indicators"
            }

            _isLoading.value = false
            _isRefreshing.value = false
        }
    }
}