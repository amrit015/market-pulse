package com.marketlabs.pulse.storage.model.summary

import com.marketlabs.pulse.utils.enums.Conviction
import com.marketlabs.pulse.utils.enums.DriverImpact
import com.marketlabs.pulse.utils.enums.IndicatorCategory
import com.marketlabs.pulse.utils.enums.MarketRegime
import com.marketlabs.pulse.utils.enums.NewsTag
import com.marketlabs.pulse.utils.enums.ReportType
import com.marketlabs.pulse.utils.enums.RiskImpactLevel
import com.marketlabs.pulse.utils.enums.SignalColor
import com.marketlabs.pulse.utils.enums.SignalDirection
import com.marketlabs.pulse.utils.enums.TechnicalSetup
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MarketPulse(
    val dateId: String,
    val lastSyncedTimestamp: Long,
    val lastUpdated: Long,
    val reportType: ReportType? = null,
    val verdict: MarketVerdict? = null,
    val drivers: List<MarketDriver>? = null,
    val position: MarketPosition? = null,
    val leadStories: List<NewsItem>? = null,
    val macroMix: List<MacroItem>? = null,
    val dominoEffect: DominoEffect? = null,
    val watch: List<WatchItem>? = null,
    val risks: List<RiskItem>? = null,
    val whatChanged: String? = null,
    // New 2026-08-21: deterministic (non-AI), most-recent-first list of indicators whose data
    // posted in the last 7 days. Can legitimately be empty on a quiet week -- mapped to
    // emptyList() rather than left null, same convention leadStories/macroMix already follow.
    val whatsNew: List<WhatsNewItem>? = null
)

// Replaces the old Verdict/TheRead split -- the backend consolidated signal + the_read into
// one verdict object (2026-08-17 backend revamp). signalLine is the top-of-screen flash,
// analysis (verdict_text) is the closing synthesis; both sections read from this one model.
// `posture` was `action`; there is no `call` (BUY/SELL/HOLD) field anymore -- removed
// project-wide backend-side since it was the one field structurally immune to the
// compliance sanitizer.
@JsonClass(generateAdapter = true)
data class MarketVerdict(
    val regime: MarketRegime? = null,
    val setup: TechnicalSetup? = null,
    val signalLine: String? = null,
    val analysis: String? = null,
    val posture: String? = null,
    val direction: SignalDirection? = null,
    val conviction: Conviction? = null,
    val convictionReason: String? = null
)

// direction is mapped straight to SignalColor in the mapper (BULLISH/BEARISH/NEUTRAL ->
// GREEN/RED/YELLOW), so this renders through the existing SignalColor.textColor/.pillColor
// extensions with no new color logic. 💡 As of 2026-08-18, direction is the model's own
// reconciled call on this driver's net effect on equities right now -- NOT a mechanical
// pass-through of the underlying indicator's own raw reading. A weak print can still be BULLISH
// here if it's fueling a dovish-pivot rally ("bad news is good news"); BEARISH means "currently
// pressuring the market down," not "this data point was bad." Don't reintroduce copy, tooltips,
// or logic that assumes the two are the same thing.
// 💡 As of 2026-08-21, direction and dataDirection are two deliberately separate reads, not a
// primary/fallback pair -- direction is the model's reconciled call on this driver's net effect
// on equities right now (see this class's doc comment above), while dataDirection is the
// indicator's own natural/deterministic reading, independent of narrative (e.g. contracting
// payrolls is always BEARISH here regardless of how the market's trading it). Both are always
// present and can legitimately disagree -- that disagreement is the interesting signal, not
// noise. TODO(design-wiring): no visual treatment for dataDirection has landed yet; it's plumbed
// through so it's available once one does, not rendered anywhere in SummaryScreen.kt today.
@JsonClass(generateAdapter = true)
data class MarketDriver(
    val label: String? = null,
    val direction: SignalColor? = null,
    val dataDirection: SignalColor? = null,
    val category: IndicatorCategory? = null,
    val impact: DriverImpact? = null
)

// Composed server-side at read time (market_indicators/positioning + ai_synthesis +
// system/market_regime) and merged into the same market_pulse response -- not a second
// network call. Every sub-block can be entirely null on cold start.
@JsonClass(generateAdapter = true)
data class MarketPosition(
    val horizons: Horizons? = null,
    val positioning: Positioning? = null,
    val valuation: Valuation? = null,
    val cycleZone: String? = null
)

@JsonClass(generateAdapter = true)
data class Horizons(
    val short: Horizon? = null,
    val medium: Horizon? = null,
    val long: Horizon? = null
)

@JsonClass(generateAdapter = true)
data class Horizon(
    val riskLevel: RiskImpactLevel? = null,
    val keyDriver: String? = null
)

// signalText/signalColor are pre-classified backend-side (indicators/positioningLogic.ts's
// classifyPositioning) -- render directly, no client-side threshold logic.
@JsonClass(generateAdapter = true)
data class Positioning(
    val pctFrom52wHigh: Double? = null,
    val pctFrom52wLow: Double? = null,
    val rangePosition: Double? = null,
    val extensionPercentile1y: Double? = null,
    val windowLabel: String? = null,
    val signalText: String? = null,
    val signalColor: SignalColor? = null
)

@JsonClass(generateAdapter = true)
data class Valuation(
    val pePercentile5y: Double? = null
)

@JsonClass(generateAdapter = true)
data class NewsItem(
    val headline: String? = null,
    val summary: String? = null
)

@JsonClass(generateAdapter = true)
data class MacroItem(
    val headline: String? = null,
    val tag: NewsTag? = null,
    val summary: String? = null
)

@JsonClass(generateAdapter = true)
data class DominoEffect(
    val trigger: String? = null,
    val impact: String? = null,
    val outlook: String? = null
)

@JsonClass(generateAdapter = true)
data class WatchItem(
    val label: String? = null,
    val timeframe: String? = null,
    val why: String? = null
)

// severity reuses RiskImpactLevel (utils/enums/RiskEnums.kt) -- the market_risk domain's
// existing HIGH/MEDIUM/LOW severity vocabulary already matches the backend's
// RISK_SEVERITY_VALUES exactly, so this doesn't duplicate it with a second enum.
@JsonClass(generateAdapter = true)
data class RiskItem(
    val risk: String? = null,
    val severity: RiskImpactLevel? = null,
    val note: String? = null
)

// One row of whats_new[] (new 2026-08-21) -- signalText/signalColor are pre-classified
// backend-side, same convention Positioning's own signalText/signalColor pair already follows:
// render directly, no client-side threshold logic. No hierarchy slot or visual design has landed
// for this section yet -- see WhatsNewSection in SummaryScreen.kt for the placeholder rendering.
@JsonClass(generateAdapter = true)
data class WhatsNewItem(
    val label: String? = null,
    val category: IndicatorCategory? = null,
    val valueDisplay: String? = null,
    val changeDisplay: String? = null,
    val signalText: String? = null,
    val signalColor: SignalColor? = null,
    val releaseDate: String? = null
)
