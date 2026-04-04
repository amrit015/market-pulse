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

data class DictionaryItem(
    val title: String,
    val subtitle: String,
    val definition: String,
    val howToRead: String,
    val observationDate: String? = null
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

    private val indicatorDefinitions = listOf(

        // ==========================================
        // 🌍 MACRO VITALS
        // ==========================================
        DictionaryItem(
            title = "Consumer Price Index (CPI)",
            subtitle = "The Headline Inflation Rate",
            definition = "Tracks the average change in prices paid by everyday consumers for a basket of goods and services like rent, food, and gas.",
            howToRead = "High CPI forces the Fed to raise interest rates, which makes borrowing expensive and crushes stock prices. Falling CPI allows the Fed to cut rates, which fuels market rallies."
        ),
        DictionaryItem(
            title = "Core PCE",
            subtitle = "The Fed's Preferred Inflation Gauge",
            definition = "Measures inflation but strips out highly volatile food and energy prices to find the true, sticky underlying trend of inflation.",
            howToRead = "The Federal Reserve bases its rate cuts entirely on this number. If Core PCE is stuck above 2.5%, the Fed cannot cut interest rates, regardless of what headline CPI says."
        ),
        DictionaryItem(
            title = "Unemployment Rate",
            subtitle = "The Labor Market Health",
            definition = "The percentage of the total labor force that is actively seeking employment but currently without a job.",
            howToRead = "A low, stable rate means consumers have money to spend. A rapidly spiking rate usually causes consumer spending to collapse, signaling an impending corporate earnings recession."
        ),
        DictionaryItem(
            title = "Nonfarm Payrolls",
            subtitle = "Monthly Job Creation",
            definition = "The total number of actual jobs added or lost in the US economy each month, excluding farm workers.",
            howToRead = "A number significantly higher than estimates implies a booming economy. A negative number or a massive miss signals severe economic contraction and triggers market sell-offs."
        ),
        DictionaryItem(
            title = "Real GDP",
            subtitle = "The Ultimate Economic Scorecard",
            definition = "Gross Domestic Product measures the total monetary value of all finished goods and services produced, adjusted to remove the illusion of inflation.",
            howToRead = "Two consecutive quarters of negative GDP growth is the traditional definition of a recession. Positive GDP confirms the economy is expanding."
        ),
        DictionaryItem(
            title = "Retail Sales",
            subtitle = "Consumer Spending Momentum",
            definition = "Because consumer spending makes up roughly 70% of the entire US economy, this acts as a real-time heartbeat monitor for the average household.",
            howToRead = "Rising sales mean consumers are confident and spending freely. Falling sales suggest consumers are tapped out, warning of an upcoming slowdown."
        ),
        DictionaryItem(
            title = "Federal Funds Rate",
            subtitle = "The Master Interest Rate",
            definition = "The rate at which banks lend money to each other. It dictates the cost of all other debt in the world, including mortgages, credit cards, and corporate borrowing.",
            howToRead = "Lower rates act as rocket fuel, inflating stock and crypto prices. Higher rates act as brakes, slowing the economy down and compressing stock valuations."
        ),

        // ==========================================
        // 🚦 MARKET PHASE (Trend, Health, Risk)
        // ==========================================
        DictionaryItem(
            title = "S&P 500 Direction",
            subtitle = "The Macro Trend",
            definition = "Evaluates the current price of the S&P 500 against its 20-day, 50-day, and 200-day moving averages.",
            howToRead = "• BULLISH: Price is above all averages. The uptrend is confirmed.\n• NEUTRAL: Price is caught between averages. The market is chopping sideways.\n• BEARISH: Price has fallen below the 200-day average. We are officially in a Bear Market."
        ),
        DictionaryItem(
            title = "Market Breadth",
            subtitle = "The Truth Serum",
            definition = "Compares the Equal-Weight S&P 500 (RSP) to the standard Market-Cap S&P 500 (SPY) to see if the average stock is actually rising.",
            howToRead = "• WIDENING: The market is up, and most individual stocks are rising (Healthy).\n• STABLE: Breadth is moving in line with the index.\n• NARROWING: The index is up, but it's only being carried by a few mega-tech stocks while everything else falls (Fake Rally)."
        ),
        DictionaryItem(
            title = "VIX (Volatility)",
            subtitle = "The Fear Gauge",
            definition = "Measures how much traders are willing to pay for 'insurance' (put options) to protect against a stock market crash over the next 30 days.",
            howToRead = "• CALM (< 20): Normal, bullish market conditions.\n• ELEVATED (20–30): The market is nervous and choppy.\n• PANIC (> 30): Extreme fear. Paradoxically, peak panic is often the absolute best time to buy stocks."
        ),
        DictionaryItem(
            title = "RSI (Momentum)",
            subtitle = "The Momentum Check",
            definition = "A mathematical score from 0 to 100 measuring recent price changes to see if a stock has moved too far, too fast.",
            howToRead = "• OVERBOUGHT (> 70): Euphoria. High risk of an imminent pullback.\n• NEUTRAL (30-70): Healthy consolidation.\n• OVERSOLD (< 30): Panic selling. High probability of a mechanical bounce."
        ),
        DictionaryItem(
            title = "10Y Treasury Yield",
            subtitle = "The Gravity of the Market",
            definition = "The interest rate the US government pays to borrow money for 10 years. It serves as the baseline for all global finance.",
            howToRead = "Think of it as gravity for stock prices.\n• RESTRICTIVE (> 4.50%): High rates pull down tech and growth stock valuations.\n• NEUTRAL: Normal conditions.\n• STIMULATIVE (< 3.50%): Low gravity encourages massive borrowing and acts as a tailwind for stocks."
        ),
        DictionaryItem(
            title = "US Dollar (DXY)",
            subtitle = "The Wrecking Ball",
            definition = "Measures the strength of the US Dollar against a basket of foreign currencies.",
            howToRead = "• WRECKING BALL (> 106): A strong dollar breaks emerging markets, hurts US corporate exports, and crushes crypto.\n• STABLE: Normal conditions.\n• SUPPORTIVE (< 100): A weak dollar is pure rocket fuel for global equities, gold, and bitcoin."
        ),
        DictionaryItem(
            title = "Credit Spreads",
            subtitle = "The Smart Money Check",
            definition = "The difference in interest rates between 'Junk Bonds' (risky companies) and 'Treasuries' (safe government debt).",
            howToRead = "Are bond investors scared of bankruptcies?\n• COMPLACENT (< 3.5%): Lenders are highly confident and money is flowing freely.\n• NORMAL: Average lending conditions.\n• STRESS (> 5.0%): Default fears are rising. Lenders are cutting off weak companies, which usually precedes a stock market drop."
        ),
        DictionaryItem(
            title = "Consumer Sentiment",
            subtitle = "Discretionary (XLY) vs Staples (XLP)",
            definition = "Compares the performance of luxury/non-essential stocks (Tesla, Amazon) against essential, boring stocks (Walmart, P&G).",
            howToRead = "• RISK ON (Rising): Consumers are confident and investors are buying growth.\n• DEFENSIVE (Falling): Investors anticipate a slowdown and are rotating into recession-proof safety."
        ),
        DictionaryItem(
            title = "Yield Curve (10Y-2Y)",
            subtitle = "The Recession Radar",
            definition = "The difference between the 10-Year Treasury yield and the 2-Year Treasury yield.",
            howToRead = "• NORMAL: 10Y pays more than 2Y (Healthy).\n• FLATTENING (< 0.2): The economy is transitioning to a warning zone.\n• INVERTED (< 0): Short-term rates pay more than long-term. This means investors are hiding in long-term bonds, flashing a classic recession signal."
        ),
        DictionaryItem(
            title = "Bond Volatility (MOVE)",
            subtitle = "The Bond Panic Gauge",
            definition = "The fear index for the U.S. Treasury market. It measures how violently bond prices are swinging.",
            howToRead = "The Bond market is 'smarter' than the Stock market. If Bonds break, Stocks will crash shortly after.\n• CALM (< 100): The bond market is stable.\n• NERVOUS (100-120): Rising macro instability.\n• PANIC (> 120): Severe bond market breakdown, often requiring Fed intervention."
        ),
        DictionaryItem(
            title = "Economic Pulse",
            subtitle = "Copper/Gold Ratio",
            definition = "The price of Copper ('Dr. Copper' - used in industry and housing) divided by the price of Gold (the ultimate fear asset).",
            howToRead = "• EXPANSION: Copper is outperforming. The world is building and manufacturing is expanding.\n• SLOWDOWN: Gold is outperforming. The world is scared, and money is fleeing to safety."
        ),
        DictionaryItem(
            title = "Inflation Proxy (Oil)",
            subtitle = "The Consumer Tax",
            definition = "The global price of crude oil, which dictates the cost of shipping, manufacturing, and gas at the pump.",
            howToRead = "• INFLATIONARY (> $90): High oil acts as a massive tax on consumers and prevents the Fed from cutting rates.\n• STABLE: Normal economic functioning.\n• DEFLATIONARY (< $60): Extremely low prices usually indicate severe global demand destruction (Recession)."
        ),
        DictionaryItem(
            title = "P/E Ratio (TTM)",
            subtitle = "The Price Tag of the Market",
            definition = "Price-to-Earnings measures how much investors are willing to pay today for $1 of corporate profit over the last year.",
            howToRead = "• EXPENSIVE (> 20): High risk. The market is priced for absolute perfection. Any bad news will cause a sharp drop.\n• FAIR (15-20): Normal historical valuations.\n• CHEAP (< 15): Severe discount, often seen during crashes and offering massive long-term upside."
        ),
        DictionaryItem(
            title = "Price-to-Book (P/B)",
            subtitle = "The Premium to Net Assets",
            definition = "Compares the market price of the S&P 500 to the actual accounting liquidation value (assets minus liabilities) of its companies.",
            howToRead = "A high P/B means investors are paying a massive premium purely for future growth promises rather than current hard assets. When this drops toward historical baselines, it signals a deep value opportunity."
        ),
        DictionaryItem(
            title = "Equity Risk Premium",
            subtitle = "Stocks vs. Safe Bonds",
            definition = "Calculated by taking the S&P 500 Earnings Yield (1 / PE) and subtracting the risk-free 10-Year Treasury Yield.",
            howToRead = "• REWARDING (> 4%): Stocks are cheap relative to bonds. Buy stocks.\n• NEUTRAL (2-4%): Average historical compensation.\n• DANGEROUS (< 2%): You are taking massive stock market volatility risk for almost zero extra reward compared to sitting in safe government bonds."
        ),
        DictionaryItem(
            title = "Dividend Yield",
            subtitle = "The Cash Return",
            definition = "The percentage of the S&P 500's price that is paid out directly to shareholders as cash dividends annually.",
            howToRead = "When stock prices fall drastically during a crash, the dividend yield mathematically spikes. This high cash payout often attracts deep-value investors, establishing a 'floor' for the market."
        ),

        // ==========================================
        // 🎯 MARKET ACTION (Tactical Confluence)
        // ==========================================
        DictionaryItem(
            title = "Fear & Greed Index",
            subtitle = "The Contrarian Trigger",
            definition = "Calculated by CNN, this blends 7 different indicators into a 0-100 score of immediate human market psychology.",
            howToRead = "Extreme Greed (>80) means the crowd is euphoric and blind to risk (time to sell). Extreme Fear (<20) means the crowd is panicking and dumping good assets (time to buy)."
        ),
        DictionaryItem(
            title = "Put/Call Ratio",
            subtitle = "Options Market Panic",
            definition = "Tracks options volume. Puts are bets the market will crash; Calls are bets it will rise.",
            howToRead = "A ratio above 1.0 means traders are aggressively buying crash insurance (Panic)—historically a strong buy signal. A ratio below 0.7 means traders are heavily leveraged on calls (Complacency)—a dangerous setup for a trap."
        ),
        DictionaryItem(
            title = "SMA Extension",
            subtitle = "The Rubber Band Effect",
            definition = "Measures the exact percentage difference between the current S&P 500 price and its 200-day Moving Average.",
            howToRead = "Price acts like a rubber band attached to the 200-day average. If price stretches >10% above the average, it is highly likely to snap back (correction). If it dips below the average, it stretches downward before violently snapping back up (recovery)."
        )
    )

    fun getDefinitionFor(apiName: String): DictionaryItem? {
        val normalizedApiName = apiName.lowercase().trim()

        return indicatorDefinitions.find { item ->
            val normalizedTitle = item.title.lowercase()

            if (normalizedTitle == normalizedApiName) return@find true

            when {
                normalizedApiName.contains("cpi") -> normalizedTitle.contains("cpi")
                normalizedApiName.contains("pce") -> normalizedTitle.contains("pce")
                normalizedApiName.contains("unemployment") -> normalizedTitle.contains("unemployment")
                normalizedApiName.contains("nonfarm") || normalizedApiName.contains("payrolls") -> normalizedTitle.contains("nonfarm")
                normalizedApiName.contains("gdp") -> normalizedTitle.contains("gdp")
                normalizedApiName.contains("retail") -> normalizedTitle.contains("retail")
                normalizedApiName.contains("federal funds") || normalizedApiName.contains("fed rate") -> normalizedTitle.contains("federal funds")
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