package com.marketlabs.pulse.ui.screens.news

import com.marketlabs.pulse.storage.model.news.MarketNews

data class NewsUiState(
    val isLoading: Boolean = false,
    val news: MarketNews? = null,
    val errorMessage: String? = null
)