package com.marketlabs.pulse.data.stockAnalysis

import kotlinx.coroutines.flow.Flow

/** Backs the Analysis screen's custom-list announcement banner's persisted dismissal state. */
interface StockAnalysisUiStateRepository {
    /** Emits `false` until the user has ever dismissed the custom-list banner; persisted after. */
    val isCustomListBannerDismissed: Flow<Boolean>

    suspend fun dismissCustomListBanner()
}
