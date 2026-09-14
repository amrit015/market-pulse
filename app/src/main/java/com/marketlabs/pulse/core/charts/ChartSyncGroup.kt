package com.marketlabs.pulse.core.charts

import com.marketlabs.pulse.utils.enums.AssetType

/**
 * Which of `system/sync_status`'s 5 chart flags a `refreshChart` call should check -- one per
 * backend batch that actually writes `market_charts` together (see `SyncManager`'s
 * `CHART_SYNC_FLAG_KEYS`). Not derivable from `AssetType` alone for [STOCKS]: an individually
 * analyzed stock and a dashboard equity/sector tile (SPY, XLK, ...) can both be `AssetType.EQUITY`-
 * ish, but they're written by two different backend jobs on two different schedules
 * (`stockAnalysisHub.ts`'s EOD fan-out vs. `dashboardEngine.ts`'s equity/sector snap) -- so callers
 * that track individually-analyzed stocks (`StockDetailViewModel`, `StockAnalysisViewModel`) pass
 * [STOCKS] directly rather than going through [fromAssetType].
 */
enum class ChartSyncGroup(val flagKey: String) {
    STOCKS("charts_stocks_updated"),
    EQUITY_SECTOR("charts_equity_sector_updated"),
    SENTIMENT("charts_sentiment_updated"),
    FUTURES_COMMODITIES("charts_futures_commodities_updated"),
    CRYPTO("charts_crypto_updated");

    companion object {
        /**
         * For dashboard-asset callers (`AssetDetailViewModel`) -- `null` for [AssetType.UNKNOWN]
         * means "no group to check," which `ChartsRepositoryImpl` treats as always-fetch rather
         * than guessing wrong.
         */
        fun fromAssetType(assetType: AssetType): ChartSyncGroup? = when (assetType) {
            AssetType.EQUITY, AssetType.SECTOR, AssetType.INDEX -> EQUITY_SECTOR
            AssetType.SENTIMENT -> SENTIMENT
            AssetType.FUTURE, AssetType.COMMODITY -> FUTURES_COMMODITIES
            AssetType.CRYPTO -> CRYPTO
            AssetType.UNKNOWN -> null
        }
    }
}
