package com.marketlabs.pulse.core.glossary

import android.content.Context

data class MarketGlossaryData(
    val regimes: List<GlossaryTerm>,
    val setups: List<GlossaryTerm>,
    val directions: List<GlossaryTerm>,
    val cycleZones: List<GlossaryTerm>,
    val actions: List<GlossaryTerm>
)

/**
 * Regime/setup/direction/cycle-zone/action definitions for `MarketGlossaryBottomSheet` (Summary's
 * "Market Status & Glossary" sheet) -- bundled as `assets/market_glossary.json`, loaded once and
 * cached in memory for the process lifetime, replacing the old hardcoded `object MarketGlossary`.
 *
 * Plain lazily-cached singleton `object` (not a Hilt `@Singleton @Inject` class like
 * `MetricGlossaryProvider`) because every call site is a deeply nested, stateless leaf composable
 * (`MarketBottomSheet.kt` itself, reached from a dozen+ Detail/Indicators/Dashboard sections with
 * no ViewModel in between) -- `get(LocalContext.current)` is a one-line call from a Composable,
 * whereas Hilt injection would mean threading this dependency through every intermediate screen's
 * ViewModel/UiState for no functional benefit over a process-cached in-memory map.
 */
object MarketGlossaryProvider {
    private const val TAG = "MarketGlossaryProvider"
    private var cache: MarketGlossaryData? = null

    fun get(context: Context): MarketGlossaryData {
        cache?.let { return it }
        val raw = loadNestedGlossaryJson(context.applicationContext, "market_glossary.json", TAG)
        return MarketGlossaryData(
            regimes = raw["regimes"].orEmpty().toGlossaryTerms(),
            setups = raw["setups"].orEmpty().toGlossaryTerms(),
            directions = raw["directions"].orEmpty().toGlossaryTerms(),
            cycleZones = raw["cycle_zones"].orEmpty().toGlossaryTerms(),
            actions = raw["actions"].orEmpty().toGlossaryTerms()
        ).also { cache = it }
    }
}
