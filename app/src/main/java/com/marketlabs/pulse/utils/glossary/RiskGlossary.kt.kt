package com.marketlabs.pulse.utils.glossary

import com.marketlabs.pulse.utils.enums.RiskStatus
import com.marketlabs.pulse.utils.enums.RiskTrend

object RiskGlossary {

    val statuses = listOf(
        GlossaryTerm(
            term = RiskStatus.SAFE.name,
            definition = "Market foundation is highly stable. Favorable conditions for broad market exposure and aggressive growth assets."
        ),
        GlossaryTerm(
            term = RiskStatus.STABLE.name,
            definition = "Market is experiencing normal background noise. Maintain current positions but avoid over-leveraging."
        ),
        GlossaryTerm(
            term = RiskStatus.CAUTION.name,
            definition = "Underlying systemic stress detected. Consider tightening stop-losses, taking partial profits, and shifting towards defensive sectors."
        ),
        GlossaryTerm(
            term = RiskStatus.DANGER.name,
            definition = "Severe market plumbing breakdown. Prioritize capital preservation, raise cash, and deploy downside hedges."
        )
    )

    val trends = listOf(
        GlossaryTerm(
            term = RiskTrend.ACCELERATING.name,
            definition = "Systemic risk factors are actively worsening. Macroeconomic plumbing is showing signs of increasing stress and volatility compared to previous readings."
        ),
        GlossaryTerm(
            term = RiskTrend.COOLING.name,
            definition = "Systemic risk factors are subsiding. Stress in the bond and credit markets is actively decreasing, creating a safer environment for equities."
        ),
        GlossaryTerm(
            term = RiskTrend.STABLE.name,
            definition = "Risk conditions are relatively unchanged. The current macro environment is maintaining its trajectory without sudden spikes in underlying stress."
        )
    )
}