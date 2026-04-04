package com.marketlabs.pulse.utils.glossary

data class GaugeDefinition(
    val whatItMeasures: String,
    val brackets: List<Pair<String, String>> // Pair<BracketName, Description>
)

object GaugeDictionary {
    val recession = GaugeDefinition(
        whatItMeasures = "Tracks the yield difference between 10-year and 2-year government bonds. Normally, locking your money up longer pays more. When short-term rates pay more (inversion), it signals investors are hoarding long-term bonds out of fear of a near-term economic crash.",
        brackets = listOf(
            "Normal" to "Short-term rates are lower than long-term rates. The economy is expanding and functioning as expected.",
            "Flat" to "The gap is closing rapidly. Economic momentum is stalling, and market uncertainty is rising.",
            "Inverted" to "Short-term rates have overtaken long-term rates. This is Wall Street's most historically accurate recession alarm.",
            "Deep Inversion" to "A severe distortion in the bond market. The system is flashing a maximum recession warning."
        )
    )

    val foundation = GaugeDefinition(
        whatItMeasures = "Often called the \"VIX for Bonds\" (the MOVE index), this measures panic in the U.S. Treasury market. Because U.S. Treasuries are the collateral that holds the entire global financial system together, instability here eventually causes violent earthquakes in the stock market.",
        brackets = listOf(
            "Stable" to "The bond market is calm. Institutional investors are comfortable with the current economic trajectory.",
            "Nervous" to "Elevated choppiness. Traders are uncertain about future inflation or Federal Reserve actions.",
            "Stress" to "High uncertainty is causing market liquidity to dry up. Lenders are getting highly nervous.",
            "Panic" to "The foundational plumbing of the market is breaking down, usually forcing emergency intervention by the Federal Reserve."
        )
    )

    val rotation = GaugeDefinition(
        whatItMeasures = "Compares where mega-funds are hiding their money: \"Offensive\" stocks (Consumer Discretionary, like Tesla and Starbucks) versus \"Defensive\" stocks (Consumer Staples, like toothpaste and groceries).",
        brackets = listOf(
            "Offensive" to "Investors are highly confident, heavily buying growth, tech, and luxury companies.",
            "Neutral" to "The market is balanced. Capital is flowing evenly without a clear defensive bias.",
            "Defensive" to "Wall Street is getting cautious and quietly moving billions into safe, boring, recession-proof necessities.",
            "Flight to Safety" to "A massive, aggressive rotation into defensive sectors, signaling extreme institutional fear of a downturn."
        )
    )

    val growthFear = GaugeDefinition(
        whatItMeasures = "The Copper-to-Gold ratio. Copper is the building block of the global economy (housing, EVs, infrastructure). Gold is the ultimate fear asset. This ratio tells us if the world is building for the future or hiding from a disaster.",
        brackets = listOf(
            "Expansion" to "Global manufacturing is humming; industrial demand for copper is massively outpacing gold.",
            "Slowdown" to "Late-cycle dynamics. Industrial demand is cooling off while investors start hedging.",
            "Risk-Aversion" to "Growth expectations are dropping rapidly while fear drives investors to hoard gold.",
            "Recessionary" to "A complete collapse of economic growth expectations relative to safe-haven demand."
        )
    )

    val canary = GaugeDefinition(
        whatItMeasures = "Tracks the \"Credit Spread\"—the extra interest that risky companies must pay to borrow money compared to the ultra-safe U.S. government. Like a canary in a coal mine, when this spikes, it means banks are terrified of bankruptcies and are choking off cash.",
        brackets = listOf(
            "Healthy" to "Money is flowing freely. Lenders are highly confident and not worried about corporate bankruptcies.",
            "Caution" to "Lenders are starting to demand slightly higher premiums for taking on corporate risk.",
            "Stress" to "Banks are getting genuinely scared, heavily tightening borrowing conditions for the broader economy.",
            "Crisis" to "Credit markets are freezing up. Riskier companies cannot get funding, making mass bankruptcies imminent."
        )
    )

    val overallScore = GaugeDefinition(
        whatItMeasures = "The Vulnerability Score is a blended macroeconomic aggregate measuring hidden systemic stress. It analyzes bond volatility, yield curve inversions, credit spreads, and institutional asset rotation. \n\n" +
                "Note: The model includes a 'Contagion Penalty'. If any single gauge flashes extreme danger, the overall score is automatically pushed higher to reflect the risk of market contagion, even if other areas appear calm.",
        brackets = listOf(
            "0 to 39 (SAFE)" to "Market foundation is highly stable. Favorable conditions for broad market exposure and aggressive growth assets.",
            "40 to 59 (STABLE)" to "Market is experiencing normal background noise. Maintain current positions but avoid over-leveraging.",
            "60 to 79 (CAUTION)" to "Underlying systemic stress detected. Consider tightening stop-losses, taking partial profits, and shifting towards defensive sectors.",
            "80 to 100 (DANGER)" to "Severe market plumbing breakdown. Prioritize capital preservation, raise cash, and deploy downside hedges."
        )
    )

    val trend = GaugeDefinition(
        whatItMeasures = "Trend measures the velocity of systemic risk by comparing today's score to the 14-day macro baseline. It helps identify if market conditions are actively deteriorating or healing, filtering out the noise of a single bad news day.",
        brackets = listOf(
            "ACCELERATING" to "Risk is rising rapidly. Macro conditions are actively deteriorating and systemic stress is building.",
            "COOLING" to "Risk is falling. Market stress is subsiding, liquidity is returning, and conditions are healing.",
            "STABLE" to "Risk is holding steady. Fluctuations are minor and there is no significant momentum in either direction."
        )
    )
}