package com.marketlabs.pulse.ui.screens.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.news.NewsRepository
import com.marketlabs.pulse.core.sync.SyncManager
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
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // Combine local states with the persistent Room stream
    val uiState: StateFlow<NewsUiState> = combine(
        repository.getNewsStream(),
        _isLoading,
        _errorMessage
    ) { newsData, loading, error ->
        NewsUiState(
            isLoading = loading,
            news = newsData,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        // 💡 FIX: Start as loading = true to prevent the initial "Empty State" flash
        initialValue = NewsUiState(isLoading = true)
    )

    /**
     * Called by the UI when the screen becomes visible.
     */
    fun onStart() {
        syncManager.startListening()
    }

    /**
     * Called by the UI when the app goes to the background.
     */
    fun onStop() {
        syncManager.stopListening()
    }

    fun refreshNews(force: Boolean = true) {
        viewModelScope.launch {
            _isLoading.update { true }
            _errorMessage.update { null }

            repository.refreshNews(force = force)
                .onFailure { error ->
                    _errorMessage.update { error.localizedMessage ?: "Failed to fetch News" }
                }

            _isLoading.update { false }
        }
    }
}