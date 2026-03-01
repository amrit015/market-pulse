package com.marketlabs.pulse.ui.screens.indicators

data class PhaseFramework(
    val phaseTitle: String,
    val focus: String,
    val indicators: String,
    val goal: String
)

data class DictionaryItem(
    val title: String,
    val subtitle: String,
    val definition: String,
    val howToRead: String
)

object IndicatorsDictionary {

    val DISCLAIMER = "Functional Logic: The Three-Phase Framework\n\nThe dashboard is divided into three distinct phases to help you distinguish between what is happening (Trend), why it’s happening (Health), and what could go wrong (Risk)."

    // 💡 THE BIG PICTURE: Matches the actual script logic
    val macroFrameworks = listOf(
        PhaseFramework(
            phaseTitle = "Phase 1: TREND (The \"What\")",
            focus = "Is the current move sustainable?",
            indicators = "S&P 500 Direction, Market Breadth, VIX (Volatility), and RSI (Momentum).",
            goal = "Determine if the price action is supported by the \"Truth Serum\" of breadth."
        ),
        PhaseFramework(
            phaseTitle = "Phase 2: HEALTH (The \"Why\")",
            focus = "Is the macro plumbing leaking?",
            indicators = "10Y Treasury Yield, US Dollar (DXY), Credit Spreads, and Consumer Sentiment.",
            goal = "Detect internal market rot before it shows up in the price."
        ),
        PhaseFramework(
            phaseTitle = "Phase 3: RISK (The \"Warning\")",
            focus = "Are the tail-risks spiking?",
            indicators = "Yield Curve (10Y-2Y), Bond Volatility (MOVE), Economic Pulse, and Inflation Proxy (Oil).",
            goal = "Identify systemic threats like recessions or liquidity crises."
        )
    )

    // 💡 INDIVIDUAL DEFINITIONS: Titles and strings now perfectly match trafficLight.ts payloads
    private val indicatorDefinitions = listOf(
        // --- TREND ---
        DictionaryItem(
            title = "S&P 500 Direction",
            subtitle = "The Macro Trend",
            definition = "The average price of the S&P 500 over the last 200 trading days.",
            howToRead = "• BULLISH: Price is above the short, medium, and long-term averages. Confirmed uptrend.\n• NEUTRAL: Price is caught between moving averages. Choppy consolidation.\n• BEARISH: Price is below the 200-day average. We are in a Bear Market."
        ),
        DictionaryItem(
            title = "Market Breadth",
            subtitle = "The Truth Serum",
            definition = "Compares how many individual stocks went UP vs. how many went DOWN across the whole market.",
            howToRead = "• WIDENING: The market is up, and most individual stocks are participating (Healthy).\n• STABLE: Breadth is moving in line with the index.\n• NARROWING: The index is up, but fewer stocks are participating (Divergence / Fake Rally)."
        ),
        DictionaryItem(
            title = "VIX (Volatility)",
            subtitle = "The Fear Gauge",
            definition = "Measures how much traders are paying for 'insurance' (options) against a stock market crash.",
            howToRead = "• CALM (< 20): Normal market conditions.\n• ELEVATED (20–30): Choppy/Nervous market.\n• PANIC (> 30): Extreme fear. Paradoxically, this is often the best time to buy."
        ),
        DictionaryItem(
            title = "RSI (Momentum)",
            subtitle = "The Momentum Check",
            definition = "A score from 0 to 100 measuring momentum to see if the price moved too far, too fast.",
            howToRead = "• OVERBOUGHT (> 70): High risk of a pullback.\n• NEUTRAL (30-70): Healthy consolidation or reset.\n• OVERSOLD (< 30): Potential buying opportunity."
        ),

        // --- HEALTH ---
        DictionaryItem(
            title = "10Y Treasury Yield",
            subtitle = "The Gravity",
            definition = "The interest rate the US government pays to borrow money for 10 years. It is the baseline for all other loans.",
            howToRead = "Think of it as gravity for stock prices.\n• RESTRICTIVE (> 4.50%): High rates act as a drag on tech/growth valuations.\n• NEUTRAL: Normal conditions.\n• STIMULATIVE (< 3.50%): Low rates encourage borrowing and act as a tailwind."
        ),
        DictionaryItem(
            title = "US Dollar (DXY)",
            subtitle = "The Wrecking Ball",
            definition = "Measures the strength of the US Dollar against foreign currencies.",
            howToRead = "• WRECKING BALL (> 106): Strong dollar breaks emerging markets and hurts US exports.\n• STABLE: Normal conditions.\n• SUPPORTIVE (< 100): Weak dollar is rocket fuel for global equities and crypto."
        ),
        DictionaryItem(
            title = "Credit Spreads",
            subtitle = "The Smart Money Check",
            definition = "The difference in interest rates between 'Junk Bonds' (risky companies) and 'Treasuries' (safe government debt).",
            howToRead = "Are bond investors scared of bankruptcies?\n• COMPLACENT (< 3.5%): Lenders are highly confident (Risk-On).\n• NORMAL: Average lending conditions.\n• STRESS (> 5.0%): Default fears are rising. Lenders are cutting off weak companies."
        ),
        DictionaryItem(
            title = "Consumer Sentiment",
            subtitle = "Discretionary (XLY) vs Staples (XLP)",
            definition = "The price of Consumer Discretionary Stocks (e.g., Tesla) divided by Consumer Staples Stocks (e.g., Walmart).",
            howToRead = "• RISK ON (Rising): Discretionary is outperforming. Consumers are confident.\n• DEFENSIVE (Falling): Staples are outperforming. Investors are rotating to safety."
        ),

        // --- RISK ---
        DictionaryItem(
            title = "Yield Curve (10Y-2Y)",
            subtitle = "The Recession Radar",
            definition = "The difference between the 10-Year Treasury yield and the 2-Year Treasury yield.",
            howToRead = "• NORMAL: 10Y is greater than 2Y (Healthy).\n• FLATTENING (< 0.2): Warning zone.\n• INVERTED (< 0): 2Y is higher than 10Y. Classic recession signal within 12 months."
        ),
        DictionaryItem(
            title = "Bond Volatility (MOVE)",
            subtitle = "The Bond Panic Gauge",
            definition = "The fear index for the Bond market. It measures how volatile US Treasury prices are.",
            howToRead = "The Bond market is 'smarter' than the Stock market. If Bonds break, Stocks will follow.\n• CALM (< 100): The bond market is calm.\n• NERVOUS (100-120): Rising instability.\n• PANIC (> 120): Severe bond market instability."
        ),
        DictionaryItem(
            title = "Economic Pulse",
            subtitle = "Copper/Gold Ratio",
            definition = "The price of Copper ('Dr. Copper' - used in industry) divided by the price of Gold (fear/safety).",
            howToRead = "• EXPANSION: Copper is outperforming. Manufacturing/Growth is expanding.\n• SLOWDOWN: Gold is outperforming. Money is fleeing to safety."
        ),
        DictionaryItem(
            title = "Inflation Proxy (Oil)",
            subtitle = "The Consumer Tax",
            definition = "The global price of crude oil, which directly impacts inflation and consumer spending.",
            howToRead = "• INFLATIONARY (> 90): High oil acts as a tax on consumers and handcuffs the Fed.\n• STABLE: Normal economic functioning.\n• DEFLATIONARY (< 60): Too low might indicate severe demand destruction (Recession)."
        )
    )

    /**
     * Smart lookup: Matches the exact incoming API names from trafficLight.ts.
     */
    fun getDefinitionFor(apiName: String): DictionaryItem? {
        val normalizedApiName = apiName.lowercase().trim()

        return indicatorDefinitions.find { item ->
            val normalizedTitle = item.title.lowercase()

            // 1. Direct match
            if (normalizedTitle == normalizedApiName) return@find true

            // 2. Substring matches to catch any slight variations from the backend
            when {
                normalizedApiName.contains("s&p") -> normalizedTitle.contains("s&p")
                normalizedApiName.contains("breadth") -> normalizedTitle.contains("breadth")
                normalizedApiName.contains("vix") -> normalizedTitle.contains("vix")
                normalizedApiName.contains("rsi") -> normalizedTitle.contains("rsi")
                normalizedApiName.contains("10y") -> normalizedTitle.contains("10y")
                normalizedApiName.contains("dxy") || normalizedApiName.contains("dollar") -> normalizedTitle.contains("dollar")
                normalizedApiName.contains("credit") || normalizedApiName.contains("spread") -> normalizedTitle.contains("credit")
                normalizedApiName.contains("consumer") || normalizedApiName.contains("sentiment") -> normalizedTitle.contains("consumer")
                normalizedApiName.contains("curve") -> normalizedTitle.contains("curve")
                normalizedApiName.contains("move") || normalizedApiName.contains("bond vol") -> normalizedTitle.contains("move")
                normalizedApiName.contains("economic pulse") || normalizedApiName.contains("copper") -> normalizedTitle.contains("economic pulse")
                normalizedApiName.contains("inflation") || normalizedApiName.contains("oil") -> normalizedTitle.contains("inflation")
                else -> false
            }
        }
    }
}