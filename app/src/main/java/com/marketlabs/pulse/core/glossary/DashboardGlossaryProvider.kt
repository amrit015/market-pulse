package com.marketlabs.pulse.core.glossary

import android.content.Context

/**
 * The Dashboard asset detail screen's own simplified RSI/MACD/Trend/SMA definitions -- deliberately
 * separate content from `StockAnalysisGlossaryProvider`'s more technical wording for the same
 * terms (Dashboard's copy is intentionally more colloquial for a tile-glance audience), previously
 * 4 hardcoded `dashboard_glossary_*_def` strings in `strings.xml`. Bundled as
 * `assets/dashboard_glossary.json`.
 */
object DashboardGlossaryProvider {
    private const val TAG = "DashboardGlossaryProvider"
    private var cache: Map<String, String>? = null

    fun definitionFor(context: Context, key: String): String? {
        val terms = cache ?: loadFlatGlossaryJson(context.applicationContext, "dashboard_glossary.json", TAG).also { cache = it }
        return terms[key]
    }
}
