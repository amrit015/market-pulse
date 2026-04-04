package com.marketlabs.pulse.utils.enums

// ==========================================
// 🎨 1. CORE COLORS & SIGNALS
// ==========================================

enum class SignalColor {
    GREEN, YELLOW, RED, UNKNOWN;

    companion object {
        fun fromString(value: String?): SignalColor {
            return when (value?.uppercase()) {
                "GREEN" -> GREEN
                "YELLOW", "LIGHT_GREEN", "ORANGE" -> YELLOW // Grouping intermediate colors
                "RED" -> RED
                else -> UNKNOWN
            }
        }
    }
}

// ==========================================
// ⚖️ 2. MARKET VERDICTS (The "Big 3")
// ==========================================

enum class MarketRegime(val label: String) {
    HEALTHY_UPTREND("HEALTHY UPTREND"),
    DISTRIBUTION_PHASE("DISTRIBUTION PHASE"),
    CRASH_OPPORTUNITY("CRASH OPPORTUNITY"),
    DANGEROUS_DOWNTREND("DANGEROUS DOWNTREND"),
    ACCUMULATION_PHASE("ACCUMULATION PHASE"),
    SIDEWAYS_RANGE("SIDEWAYS RANGE"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun fromString(value: String?): MarketRegime =
            entries.find { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class TechnicalSetup(val label: String) {
    EXHAUSTED_OVERSOLD("EXHAUSTED OVERSOLD"),
    OVERSOLD("OVERSOLD"),
    NEUTRAL_MEAN("NEUTRAL / MEAN"),
    OVERBOUGHT("OVERBOUGHT"),
    BLOW_OFF_TOP("BLOW-OFF TOP"),
    BEARISH_DIVERGENCE("BEARISH DIVERGENCE"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun fromString(value: String?): TechnicalSetup =
            entries.find { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class TradingCall(val label: String) {
    CONTRARIAN_BUY("CONTRARIAN BUY"),
    ACCUMULATE("ACCUMULATE"),
    HOLD_TRAIL_STOPS("HOLD / TRAIL STOPS"),
    CONTRARIAN_SELL("CONTRARIAN SELL"),
    SELL_AVOID("SELL & AVOID"),
    HEDGE_PROTECT("HEDGE / PROTECT"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun fromString(value: String?): TradingCall =
            entries.find { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

// ==========================================
// 🎯 3. TACTICAL ACTION & INDICATORS
// ==========================================

// Action Signal Enum (For Market Action Pillar)
enum class ActionSignal(val label: String) {
    STRONG_BUY("STRONG BUY"),
    ACCUMULATE("ACCUMULATE"),
    NEUTRAL("NEUTRAL"),
    CAUTION("CAUTION"),
    STRONG_SELL("STRONG SELL"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun fromString(value: String?): ActionSignal {
            return entries.find { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

// Indicator Signal Enum (For specific metrics like S&P 500 Direction, RSI, etc.)
enum class IndicatorSignal(val label: String) {
    BULLISH("BULLISH"),
    BEARISH("BEARISH"),
    NEUTRAL("NEUTRAL"),
    HEALTHY("HEALTHY"),
    ELEVATED("ELEVATED"),
    WARNING("WARNING"),
    CAUTION("CAUTION"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun fromString(value: String?): IndicatorSignal {
            return entries.find { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

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

// ==========================================
// 📰 4. CONTENT & REPORTS
// ==========================================

enum class ReportType(val label: String) {
    DAILY("Daily"),
    DAILY_MARKET_PULSE("Daily Market Pulse"),
    DAILY_UPDATE("Daily Update"),
    WEEKLY_LOOKAHEAD("Weekly Lookahead"),
    WEEKEND_UPDATE("Weekend Update"),
    UNKNOWN("Unknown");

    companion object {
        fun fromString(value: String?): ReportType =
            entries.find { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class NewsTag(val label: String) {
    MACRO("Macro"),
    POLITICS("Politics"),
    SECTOR("Sector"),
    CRYPTO("Crypto"),
    GEOPOLITICS("Geopolitics"),
    TECH("Tech"),
    UNKNOWN("General");

    companion object {
        fun fromString(value: String?): NewsTag =
            entries.find { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}