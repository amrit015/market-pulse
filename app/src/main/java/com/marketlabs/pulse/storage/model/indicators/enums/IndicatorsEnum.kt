package com.marketlabs.pulse.storage.model.indicators.enums

enum class SignalColor {
    GREEN, YELLOW, RED, UNKNOWN;

    companion object {
        fun fromString(value: String?): SignalColor {
            return when (value?.uppercase()) {
                "GREEN" -> GREEN
                "YELLOW" -> YELLOW
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