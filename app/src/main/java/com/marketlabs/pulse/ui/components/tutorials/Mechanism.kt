package com.marketlabs.pulse.ui.components.tutorials

import androidx.annotation.StringRes
import com.marketlabs.pulse.R

/**
 * The seven market mechanisms that each get a [MechanismCardDeck]. [routeKey] is the stable string
 * used in navigation routes (never the enum name, so renaming a constant can't break a saved route).
 */
enum class Mechanism(val routeKey: String, @StringRes val titleRes: Int) {
    TACTICAL_MOMENTUM("tactical_momentum", R.string.tutorials_gauges_category_tactical_momentum),
    SYSTEMIC_RISK("systemic_risk", R.string.tutorials_gauges_category_systemic_risk),
    VALUATION("valuation", R.string.tutorials_gauges_category_valuation),
    MACRO_VITALS("macro_vitals", R.string.tutorials_gauges_category_macro_vitals),
    POSTURE("posture", R.string.tutorials_gauges_category_posture),
    POSITIONING("positioning", R.string.tutorials_gauges_category_positioning),
    STOCK_ANALYSIS("stock_analysis", R.string.tutorials_mechanism_stock_analysis);

    companion object {
        fun fromRouteKey(key: String?): Mechanism? = entries.firstOrNull { it.routeKey == key }
    }
}
