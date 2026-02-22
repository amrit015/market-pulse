package com.marketlabs.pulse.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.news.NewsRepository
import com.marketlabs.pulse.storage.model.news.MarketNews
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    // 💡 ACTION: Track the loading state for the Pull-To-Refresh UI
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // 💡 ACTION: Expose the Room DB stream directly to the UI as a StateFlow.
    // This automatically updates the UI the millisecond the DB changes.
    val newsState: StateFlow<MarketNews?> = repository.getNewsStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // Trigger a smart fetch on launch (will skip if cache is < 15 mins old)
        refreshNews(force = false)
    }

    /**
     * Called when the user swipes down to refresh.
     * We pass force = true to bypass the 15-minute cache limit.
     */
    fun refreshNews(force: Boolean = true) {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshNews(force = force)
            _isRefreshing.value = false
        }
    }
}