package com.marketlabs.pulse.core.insights

/**
 * Which of the 2 `/insights/{pillar}/history?metric=<id>` routes a given metric id belongs to --
 * a static, closed table of 14 ids (spec: "Per-Metric Posture & Positioning Charts"), same
 * hardcoded-lookup reasoning as Indicators' `MetricHistoryPillar`. An id outside both sets resolves
 * to `null`, and callers treat that as "no history chart for this metric," not an error.
 */
enum class InsightsHistoryPillar {
    POSTURE,
    POSITIONING;

    companion object {
        private val postureIds = setOf("dark_pool_index", "net_liquidity", "naaim_exposure")
        private val positioningIds = setOf(
            "retail_sentiment",
            "institutional_positioning_es", "institutional_positioning_nq",
            "institutional_positioning_rty", "institutional_positioning_dia",
            "short_interest_spy", "short_interest_qqq", "short_interest_iwm",
            "short_interest_dia", "short_interest_rsp", "short_interest_mags"
        )

        // The only 2 genuinely daily metrics in this domain -- everything else is weekly,
        // bi-monthly, or irregular (spec section 4), so a step-after line communicates "this is what
        // it held at until the next real release" far more honestly than a smoothed curve implying a
        // trend between points that can be weeks or months apart. Same reasoning
        // `MetricHistoryPillar.isMacroCadence` uses for Indicators' own sparse metrics, just a
        // different (non-overlapping) id set -- kept as its own function here rather than folded
        // into that one, since `MetricHistoryPillar` also drives Indicators-specific endpoint
        // routing that has no equivalent in this domain.
        private val dailyIds = setOf("dark_pool_index", "net_liquidity")

        fun forMetricId(metricId: String): InsightsHistoryPillar? = when (metricId) {
            in postureIds -> POSTURE
            in positioningIds -> POSITIONING
            else -> null
        }

        fun isSparseCadence(metricId: String): Boolean =
            forMetricId(metricId) != null && metricId !in dailyIds
    }
}
