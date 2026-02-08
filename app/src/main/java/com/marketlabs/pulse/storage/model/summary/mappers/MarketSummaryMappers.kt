package com.marketlabs.pulse.storage.model.summary.mappers

import com.marketlabs.pulse.network.model.summary.NetworkDominoEffect
import com.marketlabs.pulse.network.model.summary.NetworkMacroItem
import com.marketlabs.pulse.network.model.summary.NetworkMarketLookout
import com.marketlabs.pulse.network.model.summary.NetworkMarketPulse
import com.marketlabs.pulse.network.model.summary.NetworkNewsItem
import com.marketlabs.pulse.network.model.summary.NetworkVerdict
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketLookout
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Verdict
import com.marketlabs.pulse.storage.model.summary.enums.MarketRegime
import com.marketlabs.pulse.storage.model.summary.enums.NewsTag
import com.marketlabs.pulse.storage.model.summary.enums.ReportType
import com.marketlabs.pulse.storage.model.summary.enums.TechnicalSetup
import com.marketlabs.pulse.storage.model.summary.enums.TradingCall

// Main Converter
fun NetworkMarketPulse.toDomain(): MarketPulse {
    return MarketPulse(
        // 1. Convert String -> ReportType Enum
        reportType = ReportType.from(this.reportType),
        timestamp = this.timestamp,
        verdict = this.verdict?.toDomain(),
        leadStories = this.leadStories?.map { it.toDomain() } ?: emptyList(),
        macroMix = this.macroMix?.map { it.toDomain() } ?: emptyList(),
        dominoEffect = this.dominoEffect?.toDomain(),
        marketLookout = this.marketLookout?.toDomain()
    )
}

// Helper Converters
fun NetworkVerdict.toDomain(): Verdict {
    return Verdict(
        regime = MarketRegime.from(this.regime),
        setup = TechnicalSetup.from(this.setup),
        call = TradingCall.from(this.call),
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
        tag = NewsTag.from(this.tag),
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

fun NetworkMarketLookout.toDomain(): MarketLookout {
    return MarketLookout(
        outlook = this.outlook
    )
}