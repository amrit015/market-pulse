package com.marketlabs.pulse.ui.screens.dashboard.detail

import com.marketlabs.pulse.core.glossary.MetricGlossaryBand

/**
 * Which band the asset detail page highlights as "Current" for the three sentiment readings
 * Overview links to: the backend's `rsi_status` matched against each band's label, the same way
 * Indicators' metric detail matches `signal_text`, so the highlight always follows whatever the
 * backend's classifier returned. No client-side thresholds -- a status that matches no label
 * highlights nothing.
 *
 * Fear & Greed and Put/Call share one classifier with Indicators, so their glossary bands match
 * directly; VIX's bands come from `overview_sentiment_bands.json`, written to equal Overview's
 * five-level status.
 */
internal fun currentSentimentBandIndex(status: String?, bands: List<MetricGlossaryBand>): Int? =
    bands.indexOfFirst { it.label.equals(status?.trim(), ignoreCase = true) }.takeIf { it >= 0 }
