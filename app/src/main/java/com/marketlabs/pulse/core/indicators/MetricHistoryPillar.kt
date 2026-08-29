package com.marketlabs.pulse.core.indicators

/**
 * Which of the 4 `.../history?metric=<id>` endpoints a given metric id belongs to -- the backend
 * spec gives a static, closed table of ids per route (26 total, one per tracked indicator), so
 * this is a hardcoded lookup rather than derived from `DomainUnifiedMetric.category`'s string
 * value, matching `DashboardIntradayEligibility`'s same "closed allowlist, not a live field"
 * precedent. `positioning` and `ai_synthesis` aren't chartable at all (no numeric field, or not a
 * metrics array) -- an id outside all four sets below resolves to `null`, and callers treat that
 * as "no history chart for this metric," not an error.
 */
enum class MetricHistoryPillar {
    TACTICAL_MOMENTUM,
    SYSTEMIC_RISK,
    VALUATION,
    MACRO_VITALS;

    companion object {
        private val tacticalMomentumIds = setOf("fear_and_greed", "put_call_ratio", "spy_rsi", "sma_extension", "vix")
        private val systemicRiskIds = setOf(
            "yield_curve", "credit_spreads", "move_index", "copper_gold",
            "consumer_rotation", "market_breadth", "dxy", "oil"
        )
        private val valuationIds = setOf("pe_ratio", "pb_ratio", "erp", "div_yield")
        private val macroVitalsIds = setOf(
            "cpi_yoy", "core_pce_yoy", "core_pce_mom", "unemployment", "nfp",
            "real_gdp", "retail_sales", "fed_funds", "yield_10y"
        )

        fun forMetricId(metricId: String): MetricHistoryPillar? = when (metricId) {
            in tacticalMomentumIds -> TACTICAL_MOMENTUM
            in systemicRiskIds -> SYSTEMIC_RISK
            in valuationIds -> VALUATION
            in macroVitalsIds -> MACRO_VITALS
            else -> null
        }
    }
}
