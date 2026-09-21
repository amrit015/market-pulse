package com.marketlabs.pulse.storage.model.dashboard

import com.marketlabs.pulse.storage.model.stocks.DomainDigestSection
import com.marketlabs.pulse.utils.enums.AssetType

/**
 * Represents the master state of the market (Open/Closed) and the Global Technical Summary.
 *
 * `synthesisHeadline`/`synthesisDetail`/`synthesisState` hold the market-wide narrative -- same
 * `{headline, detail}` +
 * `state: "unavailable"|"current"` shape Posture/Positioning already use for their own synthesis
 * field. `dailyDigestSections` is new market-wide AI content riding on the same `market_overview`
 * collection listener. `synthesisGeneratedAt` is `synthesis.generated_at` from that same doc --
 * when the AI pass actually ran, used for the Dashboard hero card's "Analyzed as of" line instead
 * of `lastUpdated` (this domain object's own local-sync timestamp, which reflects whenever
 * Firestore last pushed any part of the snapshot, not specifically when `technical_summary` was
 * generated).
 */
data class MarketState(
    val isEquityOpen: Boolean? = null,
    val isFuturesOpen: Boolean? = null,
    val synthesisHeadline: String? = null,
    val synthesisDetail: String? = null,
    val synthesisState: String? = null,
    val synthesisGeneratedAt: Long? = null,
    val dailyDigestSections: List<DomainDigestSection>? = null,
    val lastUpdated: Long? = null
)

/**
 * The clean, UI-ready data class representing a single tracked asset.
 */
data class AssetOverview(
    val symbol: String,
    val name: String? = null,
    val type: AssetType,
    val isInverted: Boolean? = null,

    // Core Price Data
    val price: Double? = null,
    val previousClose: Double? = null,
    val changePercent: Double? = null,

    // Technicals & AI
    val rsi: Double? = null,
    val rsiStatus: String? = null,
    val macdSignal: String? = null,
    val technicalStatus: String? = null,
    val lastUpdated: Long? = null,

    // sma
    val sma20: Double? = null,
    val sma50: Double? = null,
    val sma200: Double? = null
)