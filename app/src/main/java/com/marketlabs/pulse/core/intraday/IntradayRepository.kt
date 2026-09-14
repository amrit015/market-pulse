package com.marketlabs.pulse.core.intraday

import com.marketlabs.pulse.storage.model.intraday.IntradaySeries
import com.marketlabs.pulse.utils.enums.AssetType
import kotlinx.coroutines.flow.Flow

/**
 * Today's intraday bars, backend-polled -- replaces the old client-side Finnhub-WS-driven
 * sparkline. Deliberately skips the standard Remote/Local/Repository 5-layer scaffold
 * (`docs/architecture/overview.md` explicitly allows this when nothing needs Room caching): this data resets
 * every trading day server-side and is never meant to survive process death, so caching it
 * locally would be actively wrong, not just unnecessary.
 *
 * One unified endpoint backs both stocks (all of them, unconditionally) and the ~23-symbol
 * dashboard tile set (`DashboardIntradayEligibility` gates which dashboard symbols are worth
 * calling this for at all, since the rest are a guaranteed 404 -- stocks need no such gate).
 */
interface IntradayRepository {

    /**
     * Starts polling [symbol] while tracked, at [pollIntervalMs]. Ref-counted -- independent
     * callers (e.g. a stock's preview card and its own pushed Detail page) can track the same
     * symbol at once, and the poll only actually stops once every tracker has called
     * [untrackSymbol]; whichever caller tracks a symbol *first* wins both [assetType] and
     * [pollIntervalMs] for the whole ref-counted group, same as it already did for [assetType].
     *
     * [assetType] gates whether a given poll tick actually hits the network -- the underlying bars
     * only change while that asset class's own market is open (see `IntradayRepositoryImpl`'s
     * schedule check), so polling a closed market is pure waste, same principle as
     * `ChartsRepositoryImpl`'s cached-during-market-hours-closed check. Defaults to `EQUITY` since
     * every stock-tracking caller (`StockDetailViewModel`, `StockAnalysisViewModel`) only ever
     * tracks individual stocks; dashboard callers pass the real `AssetOverview.type`.
     *
     * [pollIntervalMs] matches the *backend's* real write cadence for whichever group [symbol]
     * belongs to -- polling faster than the backend can ever produce a new bar just re-fetches
     * identical data. Defaults to [STOCK_POLL_INTERVAL_MS] for the same reason [assetType] defaults
     * to `EQUITY`: every stock-tracking caller wants it. Not derived from [assetType] itself for the
     * same reason [ChartSyncGroup][com.marketlabs.pulse.core.charts.ChartSyncGroup] isn't either --
     * an individually-tracked stock and a dashboard equity/sector tile can share `AssetType.EQUITY`
     * but are written by two different jobs on two different cadences (`intradayPoller.ts`'s 5-min
     * stock poll vs. `dashboardEngine.ts`'s 1-min `refreshLiveDashboardPrices`), so dashboard callers
     * pass [DASHBOARD_POLL_INTERVAL_MS] explicitly.
     */
    fun trackSymbol(
        symbol: String,
        assetType: AssetType = AssetType.EQUITY,
        pollIntervalMs: Long = STOCK_POLL_INTERVAL_MS
    )

    /** Releases this caller's hold on [symbol]. No-op if this caller was never tracking it. */
    fun untrackSymbol(symbol: String)

    /**
     * `null` means no usable data yet -- never polled (404), a transient fetch failure with
     * nothing cached yet, or a stale prior-day leftover doc (see the repository impl's freshness
     * check). Callers should render nothing beyond their own static baseline in that case, not a
     * synthesized flat line -- see `SparklineChart`'s doc comment.
     */
    fun getIntradayStream(symbol: String): Flow<IntradaySeries?>

    companion object {
        /** Matches `intradayPoller.ts`'s 5-minute cron for individually-tracked stocks. */
        const val STOCK_POLL_INTERVAL_MS = 300_000L

        /** Matches `dashboardEngine.ts`'s `refreshLiveDashboardPrices` 1-minute cron for the
         *  ~23-symbol dashboard tile set (equities/sectors/futures/commodities/crypto alike). */
        const val DASHBOARD_POLL_INTERVAL_MS = 60_000L
    }
}
