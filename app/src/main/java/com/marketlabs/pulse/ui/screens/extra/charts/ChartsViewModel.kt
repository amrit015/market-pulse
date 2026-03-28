package com.marketlabs.pulse.ui.screens.extra.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChartsViewModel @Inject constructor(
) : ViewModel() {
    init {
        refreshMarketIndices()
    }

    private fun refreshMarketIndices() = viewModelScope.launch(Dispatchers.IO) {
    }
}