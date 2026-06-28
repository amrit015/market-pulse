package com.marketlabs.pulse.storage.model.dashboard

import com.marketlabs.pulse.utils.enums.AssetType

/**
 * Represents the master state of the market (Open/Closed) and the Global Technical Summary.
 */
data class MarketState(
    val isEquityOpen: Boolean? = null,
    val isFuturesOpen: Boolean? = null,
    val technicalSummary: String? = null,
    val technicalSummaryTimestamp: Long? = null,
    val lastUpdated: Long? = null
)

/**
 * The clean, UI-ready data class representing a single tracked asset.
 */
data class AssetOverview(
    val symbol: String,
    val name: String? = null,
    val description: String? = null,
    val type: AssetType,
    val isInverted: Boolean? = null,

    // Core Price Data
    val price: Double? = null,
    val previousClose: Double? = null,
    val changePercent: Double? = null,

    // Technicals & AI
    val rsi: Double? = null,
    val rsiStatus: String? = null,
    val macdSignal: String? = null,
    val technicalStatus: String? = null,
    val lastUpdated: Long? = null,

    // sma
    val sma20: Double? = null,
    val sma50: Double? = null,
    val sma200: Double? = null
)