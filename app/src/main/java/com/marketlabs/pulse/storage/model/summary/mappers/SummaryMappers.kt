package com.marketlabs.pulse.storage.model.summary.mappers

import com.marketlabs.pulse.network.model.summary.NetworkDominoEffect
import com.marketlabs.pulse.network.model.summary.NetworkMacroItem
import com.marketlabs.pulse.network.model.summary.NetworkMarketOutlook
import com.marketlabs.pulse.network.model.summary.NetworkMarketPulse
import com.marketlabs.pulse.network.model.summary.NetworkNewsItem
import com.marketlabs.pulse.network.model.summary.NetworkVerdict
import com.marketlabs.pulse.storage.database.entity.MarketPulseEntity
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketOutlook
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Verdict
import com.marketlabs.pulse.utils.enums.MarketRegime
import com.marketlabs.pulse.utils.enums.NewsTag
import com.marketlabs.pulse.utils.enums.ReportType
import com.marketlabs.pulse.utils.enums.TechnicalSetup
import com.marketlabs.pulse.utils.enums.TradingCall
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
        leadStories = this.leadStories,
        macroMix = this.macroMix,
        dominoEffect = this.dominoEffect,
        marketOutlook = this.marketOutlook
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
        leadStories = this.leadStories,
        macroMix = this.macroMix,
        dominoEffect = this.dominoEffect,
        marketOutlook = this.marketOutlook
    )
}

// Main Converter
fun NetworkMarketPulse.toDomain(): MarketPulse {
    return MarketPulse(
        // 1. Convert String -> ReportType Enum
        dateId = this.lastUpdated?.toDateIdString() ?: "",
        lastSyncedTimestamp = System.currentTimeMillis(),
        reportType = ReportType.fromString(this.reportType),
        lastUpdated = this.lastUpdated ?: 0L,
        verdict = this.verdict?.toDomain(),
        leadStories = this.leadStories?.map { it.toDomain() } ?: emptyList(),
        macroMix = this.macroMix?.map { it.toDomain() } ?: emptyList(),
        dominoEffect = this.dominoEffect?.toDomain(),
        marketOutlook = this.marketOutlook?.toDomain()
    )
}

// Helper Converters
fun NetworkVerdict.toDomain(): Verdict {
    return Verdict(
        regime = MarketRegime.fromString(this.regime),
        setup = TechnicalSetup.fromString(this.setup),
        call = TradingCall.fromString(this.call),
        analysis = this.analysis,
        action = this.action
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

fun NetworkMarketOutlook.toDomain(): MarketOutlook {
    return MarketOutlook(
        summary = this.summary
    )
}