package com.marketlabs.pulse.network.data

/**
 * Represents the technical sentiment of a market index.
 * Used to drive UI logic (colors, icons) without hardcoding them in the data layer.
 */
enum class MarketTrend {
    BULLISH,
    BEARISH,
    OVERBOUGHT,
    OVERSOLD,
    NEUTRAL,
    UNKNOWN;

    companion object {
        // Safe parser for string data from Firestore
        fun fromString(value: String?): MarketTrend {
            return when (value?.lowercase()) {
                "bullish trend" -> BULLISH
                "bearish trend" -> BEARISH
                "overbought (sell risk)" -> OVERBOUGHT
                "oversold (buy opp)" -> OVERSOLD
                else -> NEUTRAL
            }
        }
    }
}