package com.marketlabs.pulse.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.marketlabs.pulse.utils.enums.AgreementState
import com.marketlabs.pulse.utils.enums.AlignmentState
import com.marketlabs.pulse.utils.enums.CotPositioningStatus
import com.marketlabs.pulse.utils.enums.DixStatus
import com.marketlabs.pulse.utils.enums.NaaimStatus
import com.marketlabs.pulse.utils.enums.NetLiquidityStatus
import com.marketlabs.pulse.utils.enums.RetailSentimentStatus
import com.marketlabs.pulse.utils.enums.RiskImpactLevel
import com.marketlabs.pulse.utils.enums.ShiftDirection
import com.marketlabs.pulse.utils.enums.ShortInterestStatus
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

/**
 * Indicators domain, schema_version 2: `executive.alignment_with_macro` -- whether the 4 pillars'
 * stances agree with what the current macro regime would predict. Both non-ALIGNED variants are a
 * flag to look closer, not themselves a bad signal, so they read as the same neutral/caution tone
 * -- the same `signalNeutralText`/`signalNeutralPill` tokens `SignalColor.YELLOW` already uses
 * everywhere else, not `signalWarningText`/`signalWarningPill` (a deep amber-red token this app
 * otherwise only uses for `RiskImpactLevel.MEDIUM`'s graded-severity read, and too close to the
 * bearish red at small sizes). MARKET_AHEAD_OF_FUNDAMENTALS and MARKET_BEHIND_FUNDAMENTALS aren't
 * given a bullish/bearish split here -- the backend doesn't (yet) treat one as better than the
 * other, just two different directions of the same "pricing has drifted from fundamentals" flag.
 */
val AlignmentState?.textColor: Color
    @Composable get() = when (this) {
        AlignmentState.ALIGNED -> LocalPulseColors.current.signalBullishText
        AlignmentState.MARKET_AHEAD_OF_FUNDAMENTALS,
        AlignmentState.MARKET_BEHIND_FUNDAMENTALS -> LocalPulseColors.current.signalNeutralText
        AlignmentState.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

val AlignmentState?.pillColor: Color
    @Composable get() = when (this) {
        AlignmentState.ALIGNED -> LocalPulseColors.current.signalBullishPill
        AlignmentState.MARKET_AHEAD_OF_FUNDAMENTALS,
        AlignmentState.MARKET_BEHIND_FUNDAMENTALS -> LocalPulseColors.current.signalNeutralPill
        AlignmentState.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

/**
 * Indicators domain, schema_version 2: `pillar_scorecard[].agreement` -- how much a single
 * pillar's own contributing metrics agree with each other (distinct from [AlignmentState] above,
 * which compares a pillar's stance against the macro regime). DIVERGENT is a genuine warning
 * (real internal contradiction) and stays bearish red; MIXED is the neutral middle and uses the
 * same `signalNeutralText`/`signalNeutralPill` tokens [AlignmentState]'s non-ALIGNED variants use
 * above, for the same reason -- see that doc comment.
 */
val AgreementState?.textColor: Color
    @Composable get() = when (this) {
        AgreementState.ALIGNED -> LocalPulseColors.current.signalBullishText
        AgreementState.MIXED -> LocalPulseColors.current.signalNeutralText
        AgreementState.DIVERGENT -> LocalPulseColors.current.signalBearishText
        AgreementState.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

val AgreementState?.pillColor: Color
    @Composable get() = when (this) {
        AgreementState.ALIGNED -> LocalPulseColors.current.signalBullishPill
        AgreementState.MIXED -> LocalPulseColors.current.signalNeutralPill
        AgreementState.DIVERGENT -> LocalPulseColors.current.signalBearishPill
        AgreementState.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

/** Indicators domain, schema_version 2: `executive.shifts[].direction` -- a metric's day-over-day color-band move. */
val ShiftDirection?.textColor: Color
    @Composable get() = when (this) {
        ShiftDirection.IMPROVED -> LocalPulseColors.current.signalBullishText
        ShiftDirection.DETERIORATED -> LocalPulseColors.current.signalBearishText
        ShiftDirection.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

val ShiftDirection?.pillColor: Color
    @Composable get() = when (this) {
        ShiftDirection.IMPROVED -> LocalPulseColors.current.signalBullishPill
        ShiftDirection.DETERIORATED -> LocalPulseColors.current.signalBearishPill
        ShiftDirection.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

// ============================================================================
// 📊 MARKET POSTURE / POSITIONING (2026-08-26 revamp)
// ============================================================================

/** Posture: NAAIM Exposure Index -- rising manager exposure reads bullish, falling reads bearish. */
val NaaimStatus?.textColor: Color
    @Composable get() = when (this) {
        NaaimStatus.EXTREME_GREED_LEVERAGED, NaaimStatus.BULLISH -> LocalPulseColors.current.signalBullishText
        NaaimStatus.BEARISH, NaaimStatus.EXTREME_FEAR_HEDGED -> LocalPulseColors.current.signalBearishText
        NaaimStatus.NEUTRAL -> LocalPulseColors.current.signalNeutralText
        NaaimStatus.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

val NaaimStatus?.pillColor: Color
    @Composable get() = when (this) {
        NaaimStatus.EXTREME_GREED_LEVERAGED, NaaimStatus.BULLISH -> LocalPulseColors.current.signalBullishPill
        NaaimStatus.BEARISH, NaaimStatus.EXTREME_FEAR_HEDGED -> LocalPulseColors.current.signalBearishPill
        NaaimStatus.NEUTRAL -> LocalPulseColors.current.signalNeutralPill
        NaaimStatus.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

/** Posture: Dark Pool Index -- heavy institutional buying under the surface reads bullish. */
val DixStatus?.textColor: Color
    @Composable get() = when (this) {
        DixStatus.ACCUMULATION_BULLISH -> LocalPulseColors.current.signalBullishText
        DixStatus.DISTRIBUTION_BEARISH -> LocalPulseColors.current.signalBearishText
        DixStatus.NEUTRAL -> LocalPulseColors.current.signalNeutralText
        DixStatus.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

val DixStatus?.pillColor: Color
    @Composable get() = when (this) {
        DixStatus.ACCUMULATION_BULLISH -> LocalPulseColors.current.signalBullishPill
        DixStatus.DISTRIBUTION_BEARISH -> LocalPulseColors.current.signalBearishPill
        DixStatus.NEUTRAL -> LocalPulseColors.current.signalNeutralPill
        DixStatus.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

/** Posture: Net Liquidity trend -- expanding fiat liquidity is a tailwind, draining is a headwind. */
val NetLiquidityStatus?.textColor: Color
    @Composable get() = when (this) {
        NetLiquidityStatus.EXPANDING -> LocalPulseColors.current.signalBullishText
        NetLiquidityStatus.DRAINING -> LocalPulseColors.current.signalBearishText
        NetLiquidityStatus.NEUTRAL -> LocalPulseColors.current.signalNeutralText
        NetLiquidityStatus.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

val NetLiquidityStatus?.pillColor: Color
    @Composable get() = when (this) {
        NetLiquidityStatus.EXPANDING -> LocalPulseColors.current.signalBullishPill
        NetLiquidityStatus.DRAINING -> LocalPulseColors.current.signalBearishPill
        NetLiquidityStatus.NEUTRAL -> LocalPulseColors.current.signalNeutralPill
        NetLiquidityStatus.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

/**
 * Positioning: AAII retail sentiment is read CONTRARIAN, not literally -- the backend's own
 * parenthetical qualifiers say so directly ("CONTRARIAN CAUTION"/"CONTRARIAN OPPORTUNITY"). Extreme
 * retail bullishness (euphoria) is a caution flag for equities, so it reads warning/amber, not
 * bullish green; extreme retail bearishness (despondency) reads as a contrarian opportunity, so it
 * gets the bullish tone. This mirrors DirectionalChangePill's own documented VIX-style inversion
 * principle -- the raw label's sign and the color it should read as are two different things.
 */
val RetailSentimentStatus?.textColor: Color
    @Composable get() = when (this) {
        RetailSentimentStatus.EXTREME_BULLISH_CONTRARIAN_CAUTION -> LocalPulseColors.current.signalWarningText
        RetailSentimentStatus.EXTREME_BEARISH_CONTRARIAN_OPPORTUNITY -> LocalPulseColors.current.signalBullishText
        RetailSentimentStatus.NEUTRAL -> LocalPulseColors.current.signalNeutralText
        RetailSentimentStatus.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

val RetailSentimentStatus?.pillColor: Color
    @Composable get() = when (this) {
        RetailSentimentStatus.EXTREME_BULLISH_CONTRARIAN_CAUTION -> LocalPulseColors.current.signalWarningPill
        RetailSentimentStatus.EXTREME_BEARISH_CONTRARIAN_OPPORTUNITY -> LocalPulseColors.current.signalBullishPill
        RetailSentimentStatus.NEUTRAL -> LocalPulseColors.current.signalNeutralPill
        RetailSentimentStatus.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

/**
 * Positioning: CFTC COT futures positioning -- a crowded reading in EITHER direction (long or
 * short) is the backend's own flagged caution ("has historically preceded a reversal"), not a
 * directional bullish/bearish call, so both extremes share the same warning/amber tone.
 * INSUFFICIENT_HISTORY (fewer than 8 trailing weekly readings) reads as unknown/muted, same as a
 * gauge with no status at all -- there isn't yet enough of a trailing window to call it anything.
 */
val CotPositioningStatus?.textColor: Color
    @Composable get() = when (this) {
        CotPositioningStatus.EXTREME_LONG_CROWDED, CotPositioningStatus.EXTREME_SHORT_CROWDED -> LocalPulseColors.current.signalWarningText
        CotPositioningStatus.NEUTRAL -> LocalPulseColors.current.signalNeutralText
        CotPositioningStatus.INSUFFICIENT_HISTORY, CotPositioningStatus.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

val CotPositioningStatus?.pillColor: Color
    @Composable get() = when (this) {
        CotPositioningStatus.EXTREME_LONG_CROWDED, CotPositioningStatus.EXTREME_SHORT_CROWDED -> LocalPulseColors.current.signalWarningPill
        CotPositioningStatus.NEUTRAL -> LocalPulseColors.current.signalNeutralPill
        CotPositioningStatus.INSUFFICIENT_HISTORY, CotPositioningStatus.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

/**
 * Positioning: FINRA short interest on the index-ETF proxies -- ELEVATED (>=5x days-to-cover) is a
 * squeeze-risk caution flag (amber, not a directional call); shorts actively COVERING reads
 * bullish (a supportive flow leaving the market), shorts actively BUILDING reads bearish (growing
 * bearish conviction).
 */
val ShortInterestStatus?.textColor: Color
    @Composable get() = when (this) {
        ShortInterestStatus.ELEVATED_CROWDED_SHORT -> LocalPulseColors.current.signalWarningText
        ShortInterestStatus.COVERING_SHORTS_EXITING -> LocalPulseColors.current.signalBullishText
        ShortInterestStatus.BUILDING_SHORTS_ADDING -> LocalPulseColors.current.signalBearishText
        ShortInterestStatus.NEUTRAL -> LocalPulseColors.current.signalNeutralText
        ShortInterestStatus.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

val ShortInterestStatus?.pillColor: Color
    @Composable get() = when (this) {
        ShortInterestStatus.ELEVATED_CROWDED_SHORT -> LocalPulseColors.current.signalWarningPill
        ShortInterestStatus.COVERING_SHORTS_EXITING -> LocalPulseColors.current.signalBullishPill
        ShortInterestStatus.BUILDING_SHORTS_ADDING -> LocalPulseColors.current.signalBearishPill
        ShortInterestStatus.NEUTRAL -> LocalPulseColors.current.signalNeutralPill
        ShortInterestStatus.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }
