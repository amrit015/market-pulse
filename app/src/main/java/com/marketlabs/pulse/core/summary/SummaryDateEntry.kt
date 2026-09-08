package com.marketlabs.pulse.core.summary

import com.marketlabs.pulse.storage.model.summary.MarketPulse

/**
 * The result of looking up a past date on the Summary calendar strip. `cached` distinguishes a
 * date Room has never seen yet (still loading, first sync in flight) from one it has confirmed
 * has no report (`data == null`, tombstone row) -- both would otherwise collapse to the same
 * `data == null`, which isn't enough on its own to tell "loading" apart from "confirmed empty."
 */
data class SummaryDateEntry(
    val data: MarketPulse?,
    val cached: Boolean
)
