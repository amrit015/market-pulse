package com.marketlabs.pulse.storage.model.summary.enums

// 1. Market Regime
enum class MarketRegime(val label: String) {
    HEALTHY_UPTREND("HEALTHY UPTREND"),
    DISTRIBUTION_PHASE("DISTRIBUTION PHASE"),
    CRASH_OPPORTUNITY("CRASH OPPORTUNITY"),
    DANGEROUS_DOWNTREND("DANGEROUS DOWNTREND"),
    ACCUMULATION_PHASE("ACCUMULATION PHASE"),
    SIDEWAYS_RANGE("SIDEWAYS RANGE"),
    UNKNOWN("Unknown");

    companion object {
        fun from(value: String?): MarketRegime =
            entries.find { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

// 2. The Setup (Technical Status)
enum class TechnicalSetup(val label: String) {
    EXHAUSTED_OVERSOLD("EXHAUSTED OVERSOLD"),
    OVERSOLD("OVERSOLD"),
    NEUTRAL_MEAN("NEUTRAL / MEAN"),
    OVERBOUGHT("OVERBOUGHT"),
    BLOW_OFF_TOP("BLOW-OFF TOP"),
    BEARISH_DIVERGENCE("BEARISH DIVERGENCE"),
    UNKNOWN("Unknown");

    companion object {
        fun from(value: String?): TechnicalSetup =
            entries.find { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

// 3. The Call (Action)
enum class TradingCall(val label: String) {
    CONTRARIAN_BUY("CONTRARIAN BUY"),
    ACCUMULATE("ACCUMULATE"),
    HOLD_TRAIL_STOPS("HOLD / TRAIL STOPS"),
    CONTRARIAN_SELL("CONTRARIAN SELL"),
    SELL_AVOID("SELL & AVOID"),
    HEDGE_PROTECT("HEDGE / PROTECT"),
    UNKNOWN("Unknown");

    companion object {
        fun from(value: String?): TradingCall =
            entries.find { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

// 4. Report Type (Kept Title Case as per index.ts schema)
enum class ReportType(val label: String) {
    DAILY("Daily"),
    DAILY_MARKET_PULSE("Daily Market Pulse"),
    DAILY_UPDATE("Daily Update"),
    WEEKLY_LOOKAHEAD("Weekly Lookahead"),
    WEEKEND_UPDATE("Weekend Update"),
    UNKNOWN("Unknown");

    companion object {
        fun from(value: String?): ReportType =
            entries.find { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

// 5. Headline Tags (Kept Title Case as per index.ts schema)
enum class NewsTag(val label: String) {
    MACRO("Macro"),
    POLITICS("Politics"),
    SECTOR("Sector"),
    CRYPTO("Crypto"),
    GEOPOLITICS("Geopolitics"),
    TECH("Tech"),
    UNKNOWN("General");

    companion object {
        fun from(value: String?): NewsTag =
            entries.find { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}