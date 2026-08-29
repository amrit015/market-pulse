package com.marketlabs.pulse.core.glossary

import android.content.Context

data class RiskGlossaryData(
    val statuses: List<GlossaryTerm>,
    val trends: List<GlossaryTerm>
)

/**
 * Status/trend definitions for `RiskGlossaryBottomSheet` (Insights' Tail Risks glossary sheet) --
 * bundled as `assets/risk_glossary.json`, replacing the old hardcoded `object RiskGlossary`. Same
 * plain-singleton reasoning as `MarketGlossaryProvider` -- every call site is a stateless leaf
 * composable with no adjacent ViewModel.
 */
object RiskGlossaryProvider {
    private const val TAG = "RiskGlossaryProvider"
    private var cache: RiskGlossaryData? = null

    fun get(context: Context): RiskGlossaryData {
        cache?.let { return it }
        val raw = loadNestedGlossaryJson(context.applicationContext, "risk_glossary.json", TAG)
        return RiskGlossaryData(
            statuses = raw["statuses"].orEmpty().toGlossaryTerms(),
            trends = raw["trends"].orEmpty().toGlossaryTerms()
        ).also { cache = it }
    }
}
