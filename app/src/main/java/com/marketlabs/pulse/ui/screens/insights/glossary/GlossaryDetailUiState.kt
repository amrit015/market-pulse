package com.marketlabs.pulse.ui.screens.insights.glossary

import com.marketlabs.pulse.core.glossary.MetricGlossaryBand

/**
 * Drives the pushed glossary-detail page for a whole Positioning/Posture CARD (2026-08-27
 * convergence pass -- cards are the tap target now, not individual values within them). A card
 * can cover more than one glossary entry (a COT contract's % OI + percentile; a short-interest
 * instrument's days-to-cover + shares + mom-change), so `sections` is a list, each pairing one
 * entry with the display label for the value it explains -- shown as its own labeled block only
 * when there's more than one (a single-entry card's own screen title already says what it is).
 *
 * `mergedBands`/`currentBandIndex` collapse every section's bands into one deduplicated list
 * (by label) so the card's one overall status (e.g. a short-interest instrument's single
 * NEUTRAL/ELEVATED/COVERING/BUILDING read, which in the backend is computed by checking
 * days-to-cover THEN mom-change) highlights correctly even though its two underlying glossary
 * entries each carry only part of that vocabulary -- same "highlight the band matching the live
 * status" idea `MetricDetailScreen` uses for Indicators, just resolved once across every section
 * instead of one entry's own bands.
 */
data class GlossaryDetailUiState(
    val title: String,
    val description: String?,
    val sections: List<GlossarySection>,
    val mergedBands: List<MetricGlossaryBand>,
    val currentBandIndex: Int?
)

/** One glossary entry within a merged card, paired with the display label for the value it explains. */
data class GlossarySection(
    val label: String,
    val whatItIs: String,
    val howToRead: String,
    val gotchas: String?
)
