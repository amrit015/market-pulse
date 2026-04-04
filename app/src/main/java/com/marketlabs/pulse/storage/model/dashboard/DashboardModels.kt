package com.marketlabs.pulse.storage.model.dashboard

import com.marketlabs.pulse.utils.enums.AssetType

/**
 * Represents the master state of the market (Open/Closed) and the Global Technical Summary.
 */
data class MarketState(
    val isEquityOpen: Boolean?,
    val isFuturesOpen: Boolean?,
    val technicalSummary: String?, // 💡 NEW: Macro AI Summary
    val technicalSummaryTimestamp: Long?, // 💡 NEW: Analyzed at timestamp
    val lastUpdated: Long?
)

/**
 * The clean, UI-ready data class representing a single tracked asset.
 */
data class AssetOverview(
    val symbol: String,
    val name: String?,
    val description: String?,
    val type: AssetType,
    val isInverted: Boolean?,

    // Core Price Data
    val price: Double?,
    val previousClose: Double?,
    val changePercent: Double?,

    // Technicals & AI
    val rsi: Double?,
    val rsiStatus: String?,
    val macdSignal: String?,
    val technicalStatus: String?,
    val lastUpdated: Long?,

    // sma
    val sma20: Double?,
    val sma50: Double?,
    val sma200: Double?
)