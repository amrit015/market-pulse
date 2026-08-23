package com.marketlabs.pulse.utils.glossary

data class PillarGuide(
    val pillarName: String,
    val timeframe: String,
    val purpose: String,
    val howToUse: String
)

data class PhaseFramework(
    val phaseTitle: String,
    val focus: String,
    val indicators: String,
    val goal: String
)

object IndicatorsDictionary {

    val DISCLAIMER = "Functional Logic: The Three-Pillar Framework\n\nThe dashboard is divided into three distinct pillars. Together, they tell you what the economy is doing, how the market is reacting, and exactly when to execute a trade."

    val dashboardPillars = listOf(
        PillarGuide(
            pillarName = "Market Action (The \"When\")",
            timeframe = "Short-Term (Days to Weeks)",
            purpose = "The 'Sniper' tool. Measures human emotion, panic, and mathematical over-extensions.",
            howToUse = "Step 1: Look here for immediate entry and exit timing. It answers: 'Has the market moved too far, too fast? Is it time to buy the dip or sell the rip?'"
        ),
        PillarGuide(
            pillarName = "Market Phase (The \"What\")",
            timeframe = "Medium-Term (Weeks to Months)",
            purpose = "Synthesizes how the financial markets (Stocks, Bonds, Commodities) are reacting to the economy.",
            howToUse = "Step 2: Look here for portfolio positioning. If the Phase is BULLISH, hold growth stocks. If DEFENSIVE, rotate to safety. It answers: 'What season are we in?'"
        ),
        PillarGuide(
            pillarName = "Macro Vitals (The \"Why\")",
            timeframe = "Long-Term (Months to Years)",
            purpose = "Tracks the foundational bedrock of the economy using raw government data.",
            howToUse = "Step 3: Look here to understand the structural reality of the world. It answers: 'Are we in an economic expansion or heading into a recession?'"
        )
    )

    val macroFrameworks = listOf(
        PhaseFramework(
            phaseTitle = "Phase 1: TREND (The \"What\")",
            focus = "Is the current move sustainable?",
            indicators = "S&P 500 Direction, Market Breadth, VIX (Volatility), and RSI (Momentum).",
            goal = "Determine if the price action is supported by the \"Truth Serum\" of breadth, or if it is a fake rally propped up by a few big tech stocks."
        ),
        PhaseFramework(
            phaseTitle = "Phase 2: HEALTH (The \"Why\")",
            focus = "Is the macro plumbing leaking?",
            indicators = "10Y Treasury Yield, US Dollar (DXY), Credit Spreads, and Consumer Sentiment.",
            goal = "Detect internal market rot—like spiking interest rates or freezing credit—before it shows up in stock prices."
        ),
        PhaseFramework(
            phaseTitle = "Phase 3: RISK (The \"Warning\")",
            focus = "Are the tail-risks spiking?",
            indicators = "Yield Curve (10Y-2Y), Bond Volatility (MOVE), Economic Pulse, and Inflation Proxy (Oil).",
            goal = "Identify severe systemic threats like impending recessions, liquidity crises, or inflationary shocks."
        ),
        PhaseFramework(
            phaseTitle = "Phase 4: VALUATION (The \"Price\")",
            focus = "Are stocks historically expensive or cheap?",
            indicators = "P/E Ratio, Price-to-Book, Equity Risk Premium, and Dividend Yield.",
            goal = "Determine if the fundamental earnings of the companies actually justify their current stock prices."
        )
    )
}