package com.marketlabs.pulse.storage.model.indicators.enums

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

enum class VerdictCall {
    ACCUMULATE,
    HOLD_TRAIL_STOPS,
    HEDGE_PROTECT,
    SELL_AVOID,
    CONTRARIAN_BUY,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): VerdictCall {
            return when (value?.uppercase()) {
                "ACCUMULATE" -> ACCUMULATE
                "HOLD / TRAIL STOPS" -> HOLD_TRAIL_STOPS
                "HEDGE / PROTECT" -> HEDGE_PROTECT
                "SELL & AVOID" -> SELL_AVOID
                "CONTRARIAN BUY" -> CONTRARIAN_BUY
                else -> UNKNOWN
            }
        }
    }
}

// 💡 NEW: Market Regime Enum
enum class MarketRegime {
    BULL_MARKET, BEAR_MARKET, CHOP_TRANSITION, DEFENSIVE, UNKNOWN;

    companion object {
        fun fromString(value: String?): MarketRegime {
            return when (value?.uppercase()) {
                "BULL MARKET" -> BULL_MARKET
                "BEAR MARKET" -> BEAR_MARKET
                "CHOP / TRANSITION" -> CHOP_TRANSITION
                "DEFENSIVE" -> DEFENSIVE
                else -> UNKNOWN
            }
        }
    }
}

// 💡 NEW: Setup Phase Enum
enum class SetupPhase {
    BULLISH_ZONE, BEARISH_ZONE, NEUTRAL_MEAN, OVERSOLD_BOUNCE, OVERBOUGHT_CAUTION, UNKNOWN;

    companion object {
        fun fromString(value: String?): SetupPhase {
            return when (value?.uppercase()) {
                "BULLISH ZONE" -> BULLISH_ZONE
                "BEARISH ZONE" -> BEARISH_ZONE
                "NEUTRAL / MEAN" -> NEUTRAL_MEAN
                "OVERSOLD BOUNCE" -> OVERSOLD_BOUNCE
                "OVERBOUGHT / CAUTION" -> OVERBOUGHT_CAUTION
                else -> UNKNOWN
            }
        }
    }
}

// 💡 NEW: Action Signal Enum (For Market Action Pillar)
enum class ActionSignal {
    STRONG_BUY, ACCUMULATE, NEUTRAL, CAUTION, STRONG_SELL, UNKNOWN;

    companion object {
        fun fromString(value: String?): ActionSignal {
            return when (value?.uppercase()) {
                "STRONG BUY" -> STRONG_BUY
                "ACCUMULATE" -> ACCUMULATE
                "NEUTRAL" -> NEUTRAL
                "CAUTION" -> CAUTION
                "STRONG SELL" -> STRONG_SELL
                else -> UNKNOWN
            }
        }
    }
}

// 💡 NEW: Indicator Signal Enum (For specific metrics)
enum class IndicatorSignal {
    BULLISH, BEARISH, NEUTRAL, HEALTHY, ELEVATED, WARNING, CAUTION, UNKNOWN;

    companion object {
        fun fromString(value: String?): IndicatorSignal {
            return when (value?.uppercase()) {
                "BULLISH" -> BULLISH
                "BEARISH" -> BEARISH
                "NEUTRAL" -> NEUTRAL
                "HEALTHY" -> HEALTHY
                "ELEVATED" -> ELEVATED
                "WARNING" -> WARNING
                "CAUTION" -> CAUTION
                else -> UNKNOWN
            }
        }
    }
}