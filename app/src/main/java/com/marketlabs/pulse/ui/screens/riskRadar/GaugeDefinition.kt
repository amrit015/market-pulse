package com.marketlabs.pulse.ui.screens.riskRadar

data class GaugeDefinition(
    val whatItMeasures: String,
    val brackets: List<Pair<String, String>> // Pair<BracketName, Description>
)

object GaugeDictionary {
    val recession = GaugeDefinition(
        whatItMeasures = "This tracks the difference in yield between long-term and short-term U.S. government bonds. Usually, investors demand higher yields to lock their money up for longer periods.",
        brackets = listOf(
            "Normal" to "Short-term rates are lower than long-term rates, signaling healthy economic expansion.",
            "Flat" to "The gap is closing, indicating a transition phase and rising uncertainty.",
            "Inverted" to "Short-term rates have overtaken long-term rates—a classic warning sign of an impending recession.",
            "Deep Inversion" to "A severe distortion in the bond market, flashing a maximum recession warning."
        )
    )

    val foundation = GaugeDefinition(
        whatItMeasures = "Often called the \"VIX for Bonds,\" this measures the wildness and uncertainty in the U.S. Treasury market. Because Treasuries are the bedrock of global finance, instability here eventually causes earthquakes in the stock market.",
        brackets = listOf(
            "Stable" to "The bond market is calm and functioning normally.",
            "Nervous" to "Elevated choppiness and background noise.",
            "Stress" to "High uncertainty is causing liquidity to dry up; lenders are getting nervous.",
            "Panic" to "The bond market plumbing is breaking down, usually forcing emergency intervention by the Federal Reserve."
        )
    )

    val rotation = GaugeDefinition(
        whatItMeasures = "This compares where big institutions are putting their money: \"Offensive\" stocks (like Tesla and Starbucks) or \"Defensive\" stocks (like toothpaste and toilet paper).",
        brackets = listOf(
            "Offensive" to "Investors are confident and buying growth/luxury companies.",
            "Neutral" to "The market is balanced with no clear defensive rotation.",
            "Defensive" to "Wall Street is getting cautious and quietly moving money into safe, boring necessities.",
            "Flight to Safety" to "A massive, aggressive rotation into defensive stocks, signaling extreme fear of an economic downturn."
        )
    )

    val growthFear = GaugeDefinition(
        whatItMeasures = "\"Dr. Copper\" measures industrial demand and global growth, while Gold measures fear and inflation. Comparing the two tells us if the world is building or hiding.",
        brackets = listOf(
            "Expansion" to "Global manufacturing is humming; copper demand outpaces gold.",
            "Slowdown" to "Late-cycle dynamics where industrial demand is starting to cool.",
            "Risk-Aversion" to "Growth is slowing rapidly while fear drives investors to hoard gold.",
            "Recessionary" to "A complete collapse of growth expectations relative to safe-haven demand."
        )
    )

    val canary = GaugeDefinition(
        whatItMeasures = "This tracks the extra interest (premium) that riskier \"junk\" companies must pay to borrow money compared to the ultra-safe U.S. government.",
        brackets = listOf(
            "Healthy" to "Money is flowing freely; lenders are not worried about bankruptcies.",
            "Caution" to "Lenders are starting to demand slightly higher premiums for taking on risk.",
            "Stress" to "Lenders are getting genuinely scared, heavily tightening borrowing conditions.",
            "Crisis" to "Credit markets are freezing up. Riskier companies cannot get funding, making bankruptcies imminent."
        )
    )

    // Inside GaugeDictionary.kt

    val overallScore = GaugeDefinition(
        whatItMeasures = "The Vulnerability Score is a blended macroeconomic aggregate measuring hidden systemic stress in the financial plumbing. It analyzes bond volatility, yield curve inversions, credit spreads, and institutional asset rotation to detect danger before it hits the stock market.",
        brackets = listOf(
            "0 to 39 (SAFE)" to "Market foundation is highly stable. Favorable conditions for broad market exposure and growth assets.",
            "40 to 59 (STABLE)" to "Market is experiencing normal background noise. Maintain current positions but increase selectivity.",
            "60 to 79 (CAUTION)" to "Underlying systemic stress detected. Consider tightening stop-losses and shifting towards defensive sectors.",
            "80 to 100 (DANGER)" to "Severe market plumbing breakdown. Prioritize capital preservation, raise cash, and deploy downside hedges."
        )
    )

    // Add this inside your GaugeDictionary object:

    val trend = GaugeDefinition(
        whatItMeasures = "Trend measures the velocity of systemic risk by comparing today's score to yesterday's. It helps identify if market conditions are actively deteriorating or healing, filtering out minor day-to-day noise.",
        brackets = listOf(
            "ACCELERATING" to "Risk is rising rapidly. Conditions are actively deteriorating and stress is building.",
            "COOLING" to "Risk is falling. Market stress is subsiding and conditions are improving.",
            "STABLE" to "Risk is holding steady. Fluctuations are minor and there is no significant momentum in either direction."
        )
    )
}