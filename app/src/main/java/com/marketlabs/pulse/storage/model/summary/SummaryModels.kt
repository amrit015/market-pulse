package com.marketlabs.pulse.storage.model.summary

import com.marketlabs.pulse.utils.enums.MarketRegime
import com.marketlabs.pulse.utils.enums.NewsTag
import com.marketlabs.pulse.utils.enums.ReportType
import com.marketlabs.pulse.utils.enums.TechnicalSetup
import com.marketlabs.pulse.utils.enums.TradingCall
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MarketPulse(
    val dateId: String,
    val lastSyncedTimestamp: Long,
    val lastUpdated: Long,
    val reportType: ReportType? = null,
    val verdict: Verdict? = null,
    val leadStories: List<NewsItem>? = null,
    val macroMix: List<MacroItem>? = null,
    val dominoEffect: DominoEffect? = null,
    val marketOutlook: MarketOutlook? = null
)

@JsonClass(generateAdapter = true)
data class Verdict(
    val regime: MarketRegime? = null,
    val setup: TechnicalSetup? = null,
    val call: TradingCall? = null,
    val analysis: String? = null,
    val action: String? = null
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
data class MarketOutlook(
    val summary: String? = null
)