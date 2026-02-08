package com.marketlabs.pulse.storage.model.summary

import com.marketlabs.pulse.storage.model.summary.enums.MarketRegime
import com.marketlabs.pulse.storage.model.summary.enums.NewsTag
import com.marketlabs.pulse.storage.model.summary.enums.ReportType
import com.marketlabs.pulse.storage.model.summary.enums.TechnicalSetup
import com.marketlabs.pulse.storage.model.summary.enums.TradingCall

data class MarketPulse(
    val dateId: String? = null,
    val lastSyncedTimestamp: Long? = null,
    val reportType: ReportType? = null,
    val timestamp: Long? = null,
    val verdict: Verdict? = null,
    val leadStories: List<NewsItem>? = null,
    val macroMix: List<MacroItem>? = null,
    val dominoEffect: DominoEffect? = null,
    val marketLookout: MarketLookout? = null
)

data class Verdict(
    val regime: MarketRegime? = null,
    val setup: TechnicalSetup? = null,
    val call: TradingCall? = null,
    val analysis: String? = null,
    val action: String? = null
)

data class NewsItem(
    val headline: String? = null,
    val summary: String? = null
)

data class MacroItem(
    val headline: String? = null,
    val tag: NewsTag? = null,
    val summary: String? = null
)

data class DominoEffect(
    val trigger: String? = null,
    val impact: String? = null,
    val outlook: String? = null
)

data class MarketLookout(
    val outlook: String? = null
)