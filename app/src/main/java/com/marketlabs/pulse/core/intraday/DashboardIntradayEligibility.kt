package com.marketlabs.pulse.core.intraday

/**
 * The 23 dashboard symbols `refreshLiveDashboardPrices` actually writes bars for -- everything
 * else (`^VIX`, other futures, `FEAR_GREED`/`PUT_CALL`) has no live feed and is a guaranteed
 * `404` on `/intraday/:symbol`, so this exists purely to avoid polling an endpoint known in
 * advance to never return data for those symbols. Stocks need no equivalent gate -- every tracked
 * stock symbol gets bars unconditionally (backend spec Part B1).
 *
 * Hardcoded rather than derived from `AssetType`: oil/copper share `AssetType.COMMODITY` with
 * gold/silver, so type alone can't distinguish "in this set" from "not" -- this has to be an
 * explicit symbol list, matching the backend's own scope exactly. Oil/copper (`CL=F`/`HG=F`)
 * joined gold/silver in this set once the backend extended live pricing to them (previously
 * neither had a live feed at all) -- all four now share the same CME daily-maintenance-halt gap
 * (~5-6pm ET) in their bars. That gap just means fewer real bars for `SparklineChart` to draw
 * (it stretches whatever it's given across the full width, always -- see its own doc comment), not
 * something the client needs to detect or interpolate across.
 */
object DashboardIntradayEligibility {

    private val eligibleSymbols = setOf(
        "SPY", "QQQ", "IWM", "DIA", "MAGS", "RSP",
        "XLK", "XLF", "XLV", "XLY", "XLP", "XLE", "XLI", "XLU", "XLB", "XLRE", "XLC",
        "GC=F", "SI=F", "CL=F", "HG=F",
        "BTC-USD", "ETH-USD"
    )

    fun isEligible(symbol: String): Boolean = symbol in eligibleSymbols
}
