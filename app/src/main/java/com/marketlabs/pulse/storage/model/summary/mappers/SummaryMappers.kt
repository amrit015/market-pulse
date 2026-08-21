package com.marketlabs.pulse.storage.model.summary.mappers

import com.marketlabs.pulse.network.model.summary.NetworkDominoEffect
import com.marketlabs.pulse.network.model.summary.NetworkDriver
import com.marketlabs.pulse.network.model.summary.NetworkHorizon
import com.marketlabs.pulse.network.model.summary.NetworkHorizons
import com.marketlabs.pulse.network.model.summary.NetworkMacroItem
import com.marketlabs.pulse.network.model.summary.NetworkMarketPosition
import com.marketlabs.pulse.network.model.summary.NetworkMarketPulse
import com.marketlabs.pulse.network.model.summary.NetworkNewsItem
import com.marketlabs.pulse.network.model.summary.NetworkPositioning
import com.marketlabs.pulse.network.model.summary.NetworkRiskItem
import com.marketlabs.pulse.network.model.summary.NetworkValuation
import com.marketlabs.pulse.network.model.summary.NetworkVerdict
import com.marketlabs.pulse.network.model.summary.NetworkWatchItem
import com.marketlabs.pulse.network.model.summary.NetworkWhatsNewEntry
import com.marketlabs.pulse.storage.database.entity.MarketPulseEntity
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.Horizon
import com.marketlabs.pulse.storage.model.summary.Horizons
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketDriver
import com.marketlabs.pulse.storage.model.summary.MarketPosition
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.MarketVerdict
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Positioning
import com.marketlabs.pulse.storage.model.summary.RiskItem
import com.marketlabs.pulse.storage.model.summary.Valuation
import com.marketlabs.pulse.storage.model.summary.WatchItem
import com.marketlabs.pulse.storage.model.summary.WhatsNewItem
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
import com.marketlabs.pulse.utils.toDateIdString

// ============================================================================
// MAPPERS: Domain -> Entity (Saving to DB)
// ============================================================================

fun MarketPulse.toMarketPulseEntity(): MarketPulseEntity {
    return MarketPulseEntity(
        dateId = this.lastUpdated.toDateIdString(),
        lastSyncedTimestamp =  System.currentTimeMillis(),
        lastUpdated = this.lastUpdated,
        reportType = this.reportType?.label ?: "",
        verdict = this.verdict,
        drivers = this.drivers,
        position = this.position,
        leadStories = this.leadStories,
        macroMix = this.macroMix,
        dominoEffect = this.dominoEffect,
        watch = this.watch,
        risks = this.risks,
        whatChanged = this.whatChanged,
        whatsNew = this.whatsNew
    )
}

// ============================================================================
// MAPPERS: Entity -> Domain (Reading fromString DB to UI)
// ============================================================================

fun MarketPulseEntity.toDomain(): MarketPulse {
    return MarketPulse(
        dateId = this.dateId,
        reportType = ReportType.fromString(this.reportType),
        lastUpdated = this.lastUpdated,
        lastSyncedTimestamp = this.lastSyncedTimestamp,
        verdict = this.verdict,
        drivers = this.drivers,
        position = this.position,
        leadStories = this.leadStories,
        macroMix = this.macroMix,
        dominoEffect = this.dominoEffect,
        watch = this.watch,
        risks = this.risks,
        whatChanged = this.whatChanged,
        whatsNew = this.whatsNew
    )
}

// Main Converter
// 💡 rolling_narrative, macro_call, and market_outlook are all present on
// NetworkMarketPulse (declared for forward-compat) but deliberately never read here --
// they stop at the DTO layer and never reach MarketPulse/SummaryUiState. See the spec
// this domain was rebuilt against (spec-20260816-summary-android.md, §1/§3) for why:
// market_outlook is still AI-authored backend-side because system/active_cache's frozen
// contract needs it for other engines, and macro_call is a self-scoring, frequently-null
// SPY prediction never vetted for display -- rendering either would be a bug, not an
// oversight.
fun NetworkMarketPulse.toDomain(): MarketPulse {
    return MarketPulse(
        dateId = this.lastUpdated?.toDateIdString() ?: "",
        lastSyncedTimestamp = System.currentTimeMillis(),
        reportType = ReportType.fromString(this.reportType),
        lastUpdated = this.lastUpdated ?: 0L,
        verdict = this.verdict?.toDomain(),
        drivers = this.drivers?.map { it.toDomain() },
        position = this.marketPosition?.toDomain(),
        leadStories = this.leadStories?.map { it.toDomain() } ?: emptyList(),
        macroMix = this.macroMix?.map { it.toDomain() } ?: emptyList(),
        dominoEffect = this.dominoEffect?.toDomain(),
        watch = this.watch?.map { it.toDomain() },
        risks = this.risks?.map { it.toDomain() },
        whatChanged = this.whatChanged,
        whatsNew = this.whatsNew?.map { it.toDomain() } ?: emptyList()
    )
}

// Helper Converters

fun NetworkVerdict.toDomain(): MarketVerdict {
    return MarketVerdict(
        regime = MarketRegime.fromString(this.regime),
        setup = TechnicalSetup.fromString(this.setup),
        signalLine = this.signalLine,
        analysis = this.analysis,
        // 💡 Falls back to the pre-rename `action` key until the deployed backend catches up
        // with the source repo's action -> posture rename -- see NetworkVerdict's doc comment.
        posture = this.posture ?: this.action,
        direction = SignalDirection.fromString(this.direction),
        conviction = Conviction.fromString(this.conviction),
        convictionReason = this.convictionReason
    )
}

// direction/dataDirection are both resolved straight to SignalColor (never a separate Direction
// enum), sharing the one BULLISH/BEARISH/NEUTRAL mapping below. 💡 As of 2026-08-18 direction is
// the model's reconciled call on the driver's net effect on equities, not a mechanical copy of
// the underlying indicator's own reading -- see MarketDriver's doc comment in SummaryModels.kt
// for why that distinction matters (e.g. weak payrolls reading BULLISH when they're fueling a
// dovish-pivot rally). dataDirection (new 2026-08-21) is that raw reading, kept as a genuinely
// separate field rather than a fallback -- the two can legitimately disagree.
fun NetworkDriver.toDomain(): MarketDriver {
    return MarketDriver(
        label = this.label,
        direction = this.direction.toDriverSignalColor(),
        dataDirection = this.dataDirection.toDriverSignalColor(),
        category = IndicatorCategory.fromString(this.category),
        impact = DriverImpact.fromString(this.impact)
    )
}

private fun String?.toDriverSignalColor(): SignalColor = when (this?.uppercase()) {
    "BULLISH" -> SignalColor.GREEN
    "BEARISH" -> SignalColor.RED
    "NEUTRAL" -> SignalColor.YELLOW
    else -> SignalColor.UNKNOWN
}

fun NetworkMarketPosition.toDomain(): MarketPosition {
    return MarketPosition(
        horizons = this.horizons?.toDomain(),
        positioning = this.positioning?.toDomain(),
        valuation = this.valuation?.toDomain(),
        cycleZone = this.cycleZone
    )
}

fun NetworkHorizons.toDomain(): Horizons {
    return Horizons(
        short = this.short?.toDomain(),
        medium = this.medium?.toDomain(),
        long = this.long?.toDomain()
    )
}

fun NetworkHorizon.toDomain(): Horizon {
    return Horizon(
        riskLevel = RiskImpactLevel.fromString(this.riskLevel),
        keyDriver = this.keyDriver
    )
}

fun NetworkPositioning.toDomain(): Positioning {
    return Positioning(
        pctFrom52wHigh = this.pctFrom52wHigh,
        pctFrom52wLow = this.pctFrom52wLow,
        rangePosition = this.rangePosition,
        extensionPercentile1y = this.extensionPercentile1y,
        windowLabel = this.windowLabel,
        signalText = this.signalText,
        signalColor = this.signalColor?.let { SignalColor.fromString(it) }
    )
}

fun NetworkValuation.toDomain(): Valuation {
    return Valuation(
        pePercentile5y = this.pePercentile5y
    )
}

fun NetworkNewsItem.toDomain(): NewsItem {
    return NewsItem(
        headline = this.headline,
        summary = this.summary
    )
}

fun NetworkMacroItem.toDomain(): MacroItem {
    return MacroItem(
        headline = this.headline,
        tag = NewsTag.fromString(this.tag),
        summary = this.summary
    )
}

fun NetworkDominoEffect.toDomain(): DominoEffect {
    return DominoEffect(
        trigger = this.trigger,
        impact = this.impact,
        outlook = this.outlook
    )
}

fun NetworkWatchItem.toDomain(): WatchItem {
    return WatchItem(
        label = this.label,
        timeframe = this.timeframe,
        why = this.why
    )
}

fun NetworkRiskItem.toDomain(): RiskItem {
    return RiskItem(
        risk = this.risk,
        severity = RiskImpactLevel.fromString(this.severity),
        note = this.note
    )
}

// signalColor is pre-classified backend-side, same convention NetworkPositioning.toDomain()
// already follows for its own signal_color -- resolved via SignalColor.fromString (GREEN/RED/
// YELLOW), not the BULLISH/BEARISH/NEUTRAL vocabulary NetworkDriver's direction fields use.
fun NetworkWhatsNewEntry.toDomain(): WhatsNewItem {
    return WhatsNewItem(
        label = this.label,
        category = IndicatorCategory.fromString(this.category),
        valueDisplay = this.valueDisplay,
        changeDisplay = this.changeDisplay,
        signalText = this.signalText,
        signalColor = this.signalColor?.let { SignalColor.fromString(it) },
        releaseDate = this.releaseDate
    )
}
