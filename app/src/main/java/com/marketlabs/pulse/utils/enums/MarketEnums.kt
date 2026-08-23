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
        // 💡 "After-Hours" is accepted as a synonym for DAILY_UPDATE even though it's not one of
        // this enum's own labels -- the backend's source intends to always collapse after-hours
        // runs into "Daily Update" (same trading day, just generated post-close) rather than
        // surface a distinct report type to users, but the currently deployed Cloud Function is
        // still emitting the older "After-Hours" value pre-dating that change. Without this, a
        // report generated under the old deploy renders "Unknown" in the header until the next
        // backend deploy catches up.
        fun fromString(value: String?): ReportType {
            if (value.equals("After-Hours", ignoreCase = true)) return DAILY_UPDATE
            return entries.find { it.label.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
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

// ==========================================
// 🧭 5. MARKET PULSE V2 (Verdict direction/conviction, drivers, market position)
// ==========================================

// Code-derived (not AI-authored) top-of-report directional read — backend's
// system/market_regime qualifiers collapsed to one of three values.
enum class SignalDirection(val label: String) {
    RISK_ON("RISK ON"), RISK_OFF("RISK OFF"), MIXED("MIXED"), UNKNOWN("UNKNOWN");

    companion object {
        fun fromString(value: String?): SignalDirection =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

// How many of the backend's four regime qualifiers (trend/breadth/vix/rate) agree.
enum class Conviction {
    LOW, MODERATE, HIGH, UNKNOWN;

    companion object {
        fun fromString(value: String?): Conviction =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

// A driver's centrality to that run's narrative, as judged by the model at selection time.
enum class DriverImpact {
    HIGH, MODERATE, UNKNOWN;

    companion object {
        fun fromString(value: String?): DriverImpact =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

// Which of the 4 indicator pillars a driver was pulled from. TACTICAL_MOMENTUM is
// modeled for forward-compat even though the backend never selects a driver from it
// (those 5 metrics are already standalone gauges elsewhere in the app).
enum class IndicatorCategory {
    TACTICAL_MOMENTUM, SYSTEMIC_RISK, VALUATION, MACRO_ECONOMY, UNKNOWN;

    companion object {
        fun fromString(value: String?): IndicatorCategory =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

// ==========================================
// 🧭 6. INDICATOR SYNTHESIS V2 (schema_version 2 -- executive/scorecard/horizons)
// ==========================================

// Code-computed (not AI-authored) read of whether the 4 pillars' stances agree with what the
// current market_regime would predict -- see indicatorSynthesis.ts's computeAlignmentWithMacro().
// 💡 2026-08-22: backend replaced the single "TENSION" value with two directional variants --
// which side of fundamentals the market's currently pricing ahead of/behind. `label` is the
// pill's own display string, shorter than the raw enum name for the outlined alignment pill.
enum class AlignmentState(val label: String) {
    ALIGNED("ALIGNED"),
    MARKET_AHEAD_OF_FUNDAMENTALS("AHEAD OF FUNDAMENTALS"),
    MARKET_BEHIND_FUNDAMENTALS("BEHIND FUNDAMENTALS"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun fromString(value: String?): AlignmentState =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

// Code-computed day-over-day color-band change for a single metric (shiftsDetection.ts) -- the AI
// only ever writes the accompanying note, never the direction itself.
enum class ShiftDirection {
    IMPROVED, DETERIORATED, UNKNOWN;

    companion object {
        fun fromString(value: String?): ShiftDirection =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

// Code-computed (pillarRollup.ts) read of how much a pillar's own metrics agree with each other --
// distinct from AlignmentState above, which compares a pillar's stance against the macro regime.
enum class AgreementState {
    ALIGNED, MIXED, DIVERGENT, UNKNOWN;

    companion object {
        fun fromString(value: String?): AgreementState =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}