package com.marketlabs.pulse.ui.screens.indicators.views

import com.marketlabs.pulse.storage.model.indicators.DomainUnifiedMetric
import com.marketlabs.pulse.ui.components.MetricChangeDisplay
import com.marketlabs.pulse.ui.components.SentimentChange
import com.marketlabs.pulse.ui.theme.PulseColors
import kotlin.math.abs

/**
 * The change pill for the three sentiment readings that also live on Overview -- Fear & Greed,
 * VIX, and Put/Call -- decided by [SentimentChange]. Fear & Greed and Put/Call follow Overview's
 * rules exactly; VIX deliberately does not (see the VIX branch below).
 * The move is derived from the metric's current and previous raw values; VIX and Put/Call use the
 * percent change relative to the previous reading.
 *
 * Returns null for any other metric, or when the move can't be computed, so the card falls back to
 * its generic backend-formatted change.
 */
internal fun sentimentChangeFor(metric: DomainUnifiedMetric, colors: PulseColors): MetricChangeDisplay? {
    val value = metric.valueRaw
    val previous = metric.previousValueRaw
    return when (metric.id) {
        FEAR_AND_GREED_ID -> {
            if (value == null) return null
            // Falls back to the backend's own point change when no previous reading was sent.
            val previousScore = previous ?: metric.changeRaw?.let { value - it }
            SentimentChange.fearGreed(value, previousScore, colors)
        }
        VIX_ID, PUT_CALL_ID -> {
            if (value == null || previous == null || previous == 0.0) return null
            // VIX here is classified Calm/Elevated/Panic with Panic red, so unlike Overview (and
            // unlike Put/Call) a rising VIX reads red -- the pill must agree with its own card.
            SentimentChange.percent(
                changePercent = (value - previous) / abs(previous) * 100.0,
                colors = colors,
                risingIsBullish = metric.id != VIX_ID
            )
        }
        else -> null
    }
}

private const val FEAR_AND_GREED_ID = "fear_and_greed"
private const val VIX_ID = "vix"
private const val PUT_CALL_ID = "put_call_ratio"
