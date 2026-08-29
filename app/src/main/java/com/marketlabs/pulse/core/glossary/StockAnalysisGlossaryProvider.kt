package com.marketlabs.pulse.core.glossary

import android.content.Context

/**
 * Definitions for the Stock Analysis Detail screen's jargon-y metric labels (ATR, Fair Value
 * Anchor, Trailing PE, ...), surfaced via `StockAnalysisGlossaryBottomSheet`. Bundled as
 * `assets/stock_analysis_glossary.json`, replacing the old hardcoded `object StockAnalysisGlossary`.
 * Same plain-singleton reasoning as `MarketGlossaryProvider` -- called from a dozen+ Detail section
 * composables (Key Levels, Fundamentals, Macro, ...) with no adjacent ViewModel.
 *
 * Keyed by a stable internal id (`"ATR"`, `"DIST_SMA200"`, ...), not the localized display string
 * a section actually renders (`stringResource(R.string.stock_detail_metric_atr)`) -- decouples the
 * lookup from string-resource wording/casing changes.
 */
object StockAnalysisGlossaryProvider {
    private const val TAG = "StockAnalysisGlossaryProvider"
    private var cache: Map<String, String>? = null

    fun definitionFor(context: Context, key: String): String? {
        val terms = cache ?: loadFlatGlossaryJson(context.applicationContext, "stock_analysis_glossary.json", TAG).also { cache = it }
        return terms[key]
    }
}
