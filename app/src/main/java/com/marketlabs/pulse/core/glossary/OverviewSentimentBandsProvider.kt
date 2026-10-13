package com.marketlabs.pulse.core.glossary

import android.content.Context

/**
 * Band lists for the Overview-only readings whose Fear/Greed levels differ from their Indicators
 * counterpart, bundled as `assets/overview_sentiment_bands.json` (`symbol -> label -> meaning`,
 * ordered low reading to high).
 *
 * Today that is VIX alone: Indicators classifies it Calm / Elevated / Panic, while Overview's
 * status is a five-level Fear/Greed read (`rsi_status` from the backend dashboard engine), so the
 * Overview detail page needs its own list. Labels are written to equal that `rsi_status` set so
 * the current band can be matched by label, like every other band list in the app.
 */
object OverviewSentimentBandsProvider {
    private const val TAG = "OverviewSentimentBands"
    private var cache: Map<String, Map<String, String>>? = null

    fun bandsFor(context: Context, symbol: String): List<MetricGlossaryBand> {
        val all = cache
            ?: loadNestedGlossaryJson(context.applicationContext, "overview_sentiment_bands.json", TAG).also { cache = it }
        return all[symbol].orEmpty().map { (label, meaning) -> MetricGlossaryBand(label, meaning) }
    }
}
