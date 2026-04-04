package com.marketlabs.pulse.utils.glossary

import com.marketlabs.pulse.utils.enums.ActionSignal
import com.marketlabs.pulse.utils.enums.MarketRegime
import com.marketlabs.pulse.utils.enums.TechnicalSetup
import com.marketlabs.pulse.utils.enums.TradingCall

// Data class to hold the dictionary terms
data class GlossaryTerm(
    val term: String,
    val definition: String
)

object MarketGlossary {

    val regimes = listOf(
        GlossaryTerm(MarketRegime.HEALTHY_UPTREND.label, "The market is steadily rising with low volatility and broad participation across sectors. A safe environment for long positions."),
        GlossaryTerm(MarketRegime.DISTRIBUTION_PHASE.label, "The market remains near its highs, but smart money is secretly selling into strength. Volatility is rising beneath the surface."),
        GlossaryTerm(MarketRegime.CRASH_OPPORTUNITY.label, "Absolute panic and capitulation. The market is flashing a rare, high-conviction contrarian buying opportunity."),
        GlossaryTerm(MarketRegime.DANGEROUS_DOWNTREND.label, "The market is in a sustained decline characterized by lower highs and lower lows. Cash is the safest position."),
        GlossaryTerm(MarketRegime.ACCUMULATION_PHASE.label, "Deep fear is subsiding, and institutional investors are quietly buying high-quality assets at discounted prices."),
        GlossaryTerm(MarketRegime.SIDEWAYS_RANGE.label, "The market is trendless, chopping back and forth between support and resistance as it awaits a macro catalyst.")
    )

    val setups = listOf(
        GlossaryTerm(TechnicalSetup.EXHAUSTED_OVERSOLD.label, "Sellers are entirely depleted. An extreme mechanical bounce is highly probable."),
        GlossaryTerm(TechnicalSetup.OVERSOLD.label, "The asset is trading well below its recent historical average, offering a good entry zone."),
        GlossaryTerm(TechnicalSetup.NEUTRAL_MEAN.label, "Price is resting near its average. There is no clear momentum edge in either direction."),
        GlossaryTerm(TechnicalSetup.OVERBOUGHT.label, "The asset has rallied too far, too fast. A short-term pullback is likely."),
        GlossaryTerm(TechnicalSetup.BLOW_OFF_TOP.label, "A euphoric buying climax. It is extremely dangerous to chase prices at these levels."),
        GlossaryTerm(TechnicalSetup.BEARISH_DIVERGENCE.label, "Price is making new highs, but underlying momentum and participation are failing.")
    )

    val calls = listOf(
        GlossaryTerm(TradingCall.CONTRARIAN_BUY.label, "Begin aggressively buying high-quality assets at deep discounts while the crowd is panicking."),
        GlossaryTerm(TradingCall.ACCUMULATE.label, "Deploy capital steadily into market leaders on minor dips."),
        GlossaryTerm(TradingCall.HOLD_TRAIL_STOPS.label, "Maintain your current exposure but tighten stop losses to protect profits."),
        GlossaryTerm(TradingCall.HEDGE_PROTECT.label, "Reduce leverage, buy protective puts, and increase cash reserves."),
        GlossaryTerm(TradingCall.SELL_AVOID.label, "Exit long positions. Capital preservation is the priority."),
        GlossaryTerm(TradingCall.CONTRARIAN_SELL.label, "The market is in absolute euphoria. Take profits immediately and rotate to defensive positioning.")
    )

    val actions = listOf(
        GlossaryTerm(ActionSignal.STRONG_BUY.label, "Extreme capitulation detected. High probability of a mechanical bounce."),
        GlossaryTerm(ActionSignal.ACCUMULATE.label, "Market is cooling off. Good zone for dollar-cost averaging."),
        GlossaryTerm(ActionSignal.NEUTRAL.label, "Market is balanced. Hold current positions."),
        GlossaryTerm(ActionSignal.CAUTION.label, "Market is heavily extended. Consider trimming profits or tightening stops."),
        GlossaryTerm(ActionSignal.STRONG_SELL.label, "Euphoria and overbought technicals. High probability of a violent pullback.")
    )
}