package com.marketlabs.pulse.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.marketIndex.MarketIndexRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val marketIndexRepository: MarketIndexRepository
) : ViewModel() {
    init {
        refreshMarketIndices()
    }

    private fun refreshMarketIndices() = viewModelScope.launch(Dispatchers.IO) {
        marketIndexRepository.refreshMarketIndicesData()
    }
}