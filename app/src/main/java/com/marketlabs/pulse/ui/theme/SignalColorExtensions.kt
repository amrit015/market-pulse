package com.marketlabs.pulse.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.marketlabs.pulse.utils.enums.RiskImpactLevel
import com.marketlabs.pulse.utils.enums.SignalColor

/**
 * 💡 THOUGHT PROCESS:
 * Replaces `utils/extensions/ColorExtension.kt`'s `SignalColor.toColor()`/`.toBgColor()` — same
 * mapping target (the domain enum), new token source (`LocalPulseColors` instead of the deleted
 * `PulseStatusColors`) and new names (`.textColor`/`.pillColor`, matching the `signal.*.text` /
 * `signal.*.pill` naming used throughout this theming system instead of the old ambiguous "Bg"
 * suffix — a "text" color and a "pill" background read unambiguously; a "Bg" color could mean
 * either).
 *
 * `UNKNOWN`/`null` fall back to `LocalPulseColors.current.signalUnknown` — a real color both
 * variants can share, rather than reaching into `MaterialTheme.colorScheme` for an unrelated
 * fallback the way the old extensions did.
 */
val SignalColor?.textColor: Color
    @Composable get() = when (this) {
        SignalColor.GREEN -> LocalPulseColors.current.signalBullishText
        SignalColor.YELLOW -> LocalPulseColors.current.signalNeutralText
        SignalColor.RED -> LocalPulseColors.current.signalBearishText
        SignalColor.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

val SignalColor?.pillColor: Color
    @Composable get() = when (this) {
        SignalColor.GREEN -> LocalPulseColors.current.signalBullishPill
        SignalColor.YELLOW -> LocalPulseColors.current.signalNeutralPill
        SignalColor.RED -> LocalPulseColors.current.signalBearishPill
        SignalColor.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

/**
 * Same text/pill pairing as [SignalColor] above, for the market_pulse Summary domain's
 * `RiskImpactLevel` fields (horizon risk_level, risk-item severity) -- a severity read (how bad)
 * rather than a bullish/bearish direction, but it maps onto the same three signal buckets: HIGH
 * severity reads as bearish/concerning, LOW reads as bullish/safe, MEDIUM as the neutral/warning
 * middle. EXTREME shares HIGH's treatment -- this domain never emits it (RISK_SEVERITY_VALUES is
 * HIGH/MEDIUM/LOW only), it's kept here purely because `RiskImpactLevel.fromString` already
 * recognizes "EXTREME"/"CRITICAL"/"SEVERE" as synonyms for it from the market_risk domain.
 */
val RiskImpactLevel?.textColor: Color
    @Composable get() = when (this) {
        RiskImpactLevel.EXTREME, RiskImpactLevel.HIGH -> LocalPulseColors.current.signalBearishText
        RiskImpactLevel.MEDIUM -> LocalPulseColors.current.signalWarningText
        RiskImpactLevel.LOW -> LocalPulseColors.current.signalBullishText
        RiskImpactLevel.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

val RiskImpactLevel?.pillColor: Color
    @Composable get() = when (this) {
        RiskImpactLevel.EXTREME, RiskImpactLevel.HIGH -> LocalPulseColors.current.signalBearishPill
        RiskImpactLevel.MEDIUM -> LocalPulseColors.current.signalWarningPill
        RiskImpactLevel.LOW -> LocalPulseColors.current.signalBullishPill
        RiskImpactLevel.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }
