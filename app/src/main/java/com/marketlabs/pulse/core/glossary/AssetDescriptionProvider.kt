package com.marketlabs.pulse.core.glossary

import android.content.Context

/**
 * Client-side replacement for `market_overview/{symbol}.description`, removed from the backend
 * doc entirely (hard cutover, no dual-write) -- the per-asset blurb on `AssetDetailScreen` now
 * comes from this bundled copy instead of the network. Backend's original strings weren't pulled
 * into this pass; these are written fresh, scoped to exactly the fixed asset set Dashboard already
 * renders (`DashboardScreen.kt`'s `equitySortOrder`/`cryptoCommoditySortOrder`/`futureSortOrder`/
 * `sectorSymbols` plus the three sentiment assets) -- extend `assets/asset_descriptions.json`
 * directly if a new symbol is added to Dashboard. Same flat `symbol -> description` shape as
 * `DashboardGlossaryProvider` (a different, unrelated glossary on the same screen -- RSI/MACD/
 * Trend/SMA term definitions, not asset blurbs), so it reuses the same loader.
 */
object AssetDescriptionProvider {
    private const val TAG = "AssetDescriptionProvider"
    private var cache: Map<String, String>? = null

    fun descriptionFor(context: Context, symbol: String): String? {
        val descriptions = cache
            ?: loadFlatGlossaryJson(context.applicationContext, "asset_descriptions.json", TAG).also { cache = it }
        return descriptions[symbol]
    }
}
