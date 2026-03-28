package com.marketlabs.pulse.ui.screens.indicators

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

data class DictionaryItem(
    val title: String,
    val subtitle: String,
    val definition: String,
    val howToRead: String
)

object IndicatorsDictionary {

    val DISCLAIMER = "Functional Logic: The Three-Pillar Framework\n\nThe dashboard is divided into three distinct pillars. Together, they tell you what the economy is doing, how the market is reacting, and exactly when to execute a trade."

    // 💡 NEW: High-level guide on how the user should utilize the entire dashboard
    val dashboardPillars = listOf(
        PillarGuide(
            pillarName = "Market Action (The \"When\")",
            timeframe = "Short-Term (Days to Weeks)",
            purpose = "The 'Sniper' tool. Measures human emotion, panic, and mathematical over-extensions.",
            howToUse = "Step 1: Look here for immediate entry and exit timing. It answers: 'Has the market moved too far, too fast? Is it time to buy the dip?'"
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
            howToUse = "Step 3: Look here to understand the structural reality of the world. It answers: 'Are we in an economic expansion or a recession?'"
        )
    )

    // 💡 THE BIG PICTURE: Matches the actual Market Phase script logic
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
        ),
        PhaseFramework(
            phaseTitle = "Phase 4: VALUATION (The \"Price\")",
            focus = "Are stocks historically expensive or cheap?",
            indicators = "P/E Ratio, Price-to-Sales, Equity Risk Premium, and Dividend Yield.",
            goal = "Determine if the fundamental earnings of the market justify the current price."
        )
    )

    // 💡 INDIVIDUAL DEFINITIONS: Combined for Vitals, Phase, and Action
    private val indicatorDefinitions = listOf(

        // ==========================================
        // 🌍 MACRO VITALS
        // ==========================================
        DictionaryItem(
            title = "Consumer Price Index (CPI)",
            subtitle = "The Headline Inflation Rate",
            definition = "Tracks the average change in prices paid by urban consumers for a basket of goods and services.",
            howToRead = "High or rising CPI forces the Fed to keep interest rates high (bearish for stocks). Falling CPI allows the Fed to cut rates (bullish)."
        ),
        DictionaryItem(
            title = "Core PCE",
            subtitle = "The Fed's Preferred Inflation Gauge",
            definition = "Measures inflation but strips out highly volatile food and energy prices to find the true underlying trend.",
            howToRead = "If Core PCE is stuck above 2.5%, the Fed is unlikely to cut interest rates, regardless of what headline CPI says."
        ),
        DictionaryItem(
            title = "Unemployment Rate",
            subtitle = "The Labor Market Health",
            definition = "The percentage of the total labor force that is actively seeking employment but currently unemployed.",
            howToRead = "A low, stable rate means a healthy consumer. A rapidly spiking rate usually signals an impending corporate earnings recession."
        ),
        DictionaryItem(
            title = "Nonfarm Payrolls",
            subtitle = "Monthly Job Creation",
            definition = "The total number of jobs added or lost in the US economy each month.",
            howToRead = "A number significantly higher than estimates implies a booming economy. A negative number signals severe economic contraction."
        ),
        DictionaryItem(
            title = "Real GDP",
            subtitle = "The Ultimate Economic Scorecard",
            definition = "Gross Domestic Product measures the total monetary value of all finished goods and services produced, adjusted for inflation.",
            howToRead = "Two consecutive quarters of negative GDP growth is the traditional definition of a recession. Positive GDP confirms expansion."
        ),
        DictionaryItem(
            title = "Retail Sales",
            subtitle = "Consumer Spending Momentum",
            definition = "Because consumer spending makes up roughly 70% of the US economy, this acts as a real-time pulse on the average household.",
            howToRead = "Rising sales mean consumers are confident and spending. Falling sales suggest consumers are tapping out."
        ),
        DictionaryItem(
            title = "Federal Funds Rate",
            subtitle = "The Master Interest Rate",
            definition = "The rate at which banks lend money to each other. It dictates the cost of all other debt in the world (mortgages, corporate borrowing).",
            howToRead = "Lower rates inflate asset prices. Higher rates slow the economy down and compress stock valuations."
        ),

        // ==========================================
        // 🚦 MARKET PHASE (Trend, Health, Risk)
        // ==========================================
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
            title = "RSI (Momentum)", // Used by both Market Phase and Market Action
            subtitle = "The Momentum Check",
            definition = "A score from 0 to 100 measuring momentum to see if the price moved too far, too fast.",
            howToRead = "• OVERBOUGHT (> 70): High risk of a pullback.\n• NEUTRAL (30-70): Healthy consolidation or reset.\n• OVERSOLD (< 30): Potential buying opportunity."
        ),
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
        ),
        DictionaryItem(
            title = "P/E Ratio (TTM)",
            subtitle = "The Price Tag of the Market",
            definition = "Price-to-Earnings measures how much investors are willing to pay for $1 of corporate profit over the trailing twelve months.",
            howToRead = "• EXPENSIVE (> 20): High risk. The market is priced for perfection.\n• FAIR (15-20): Normal historical valuations.\n• CHEAP (< 15): Severe discount, often seen during crashes."
        ),
        DictionaryItem(
            title = "Price-to-Book (P/B)",
            subtitle = "The Premium to Net Assets",
            definition = "Compares the market price of the S&P 500 to the actual accounting book value (assets minus liabilities) of its underlying companies.",
            howToRead = "A high P/B means investors are willing to pay a massive premium for future growth rather than current hard assets. A P/B ratio falling toward historical baselines often signals a value opportunity."
        ),
        DictionaryItem(
            title = "Equity Risk Premium",
            subtitle = "Stocks vs. Safe Bonds",
            definition = "Calculated by taking the S&P 500 Earnings Yield (1 / PE) and subtracting the 10-Year Treasury Yield.",
            howToRead = "• REWARDING (> 4%): Stocks are cheap relative to bonds. Buy stocks.\n• NEUTRAL (2-4%): Average historical compensation.\n• DANGEROUS (< 2%): You are taking massive stock market risk for almost zero extra yield compared to risk-free government bonds."
        ),
        DictionaryItem(
            title = "Dividend Yield",
            subtitle = "The Cash Return",
            definition = "The percentage of the S&P 500's price that is paid out to shareholders as cash dividends annually.",
            howToRead = "When stock prices fall drastically, the dividend yield spikes, often attracting value investors to establish a market floor."
        ),

        // ==========================================
        // 🎯 MARKET ACTION (Tactical Confluence)
        // ==========================================
        DictionaryItem(
            title = "Fear & Greed Index",
            subtitle = "The Contrarian Trigger",
            definition = "Calculated by CNN, this blends 7 different indicators into a 0-100 score of immediate market psychology.",
            howToRead = "Extreme Greed (>80) suggests the market is euphoric and due for a pullback. Extreme Fear (<20) often signals temporary capitulation and a buying opportunity."
        ),
        DictionaryItem(
            title = "Put/Call Ratio",
            subtitle = "Options Market Panic",
            definition = "Tracks options volume. Puts are bets the market will fall; Calls are bets it will rise.",
            howToRead = "A ratio above 1.0 (Panic) is historically a strong buy signal. A ratio below 0.7 (Complacency) suggests the market is dangerously overconfident."
        ),
        DictionaryItem(
            title = "SMA Extension",
            subtitle = "The Rubber Band Effect",
            definition = "Measures the percentage difference between the current S&P 500 price and its 200-day Moving Average.",
            howToRead = "If price is > 10% to 15% above the average, it is highly overextended (caution). If it touches or dips just below the average, it often finds massive institutional support."
        )
    )

    /**
     * Smart lookup: Matches the exact incoming API names from backend scripts.
     */
    fun getDefinitionFor(apiName: String): DictionaryItem? {
        val normalizedApiName = apiName.lowercase().trim()

        return indicatorDefinitions.find { item ->
            val normalizedTitle = item.title.lowercase()

            // 1. Direct match
            if (normalizedTitle == normalizedApiName) return@find true

            // 2. Substring matches to catch variations from the backend
            when {
                // Vitals
                normalizedApiName.contains("cpi") -> normalizedTitle.contains("cpi")
                normalizedApiName.contains("pce") -> normalizedTitle.contains("pce")
                normalizedApiName.contains("unemployment") -> normalizedTitle.contains("unemployment")
                normalizedApiName.contains("nonfarm") || normalizedApiName.contains("payrolls") -> normalizedTitle.contains("nonfarm")
                normalizedApiName.contains("gdp") -> normalizedTitle.contains("gdp")
                normalizedApiName.contains("retail") -> normalizedTitle.contains("retail")
                normalizedApiName.contains("federal funds") || normalizedApiName.contains("fed rate") -> normalizedTitle.contains("federal funds")

                // Phase & Action
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
                normalizedApiName.contains("fear") && normalizedApiName.contains("greed") -> normalizedTitle.contains("fear & greed")
                normalizedApiName.contains("put") && normalizedApiName.contains("call") -> normalizedTitle.contains("put/call")
                normalizedApiName.contains("sma extension") || normalizedApiName.contains("extension") -> normalizedTitle.contains("sma extension")
                else -> false
            }
        }
    }
}