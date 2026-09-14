# Price charts + sparklines — caching, freshness, live polling

> **Status: LIVING.** Consolidates a caching/freshness overhaul done 2026-09-13/14 to
> `ChartsRepository`/`IntradayRepository`, through several rounds of real bugs and a couple of
> abandoned approaches — read "Bugs hit and dead ends" before touching this system again, more than
> one thing here looks reasonable and isn't. Companion to `@docs/architecture/history-charts.md`
> (a *different* chart system — per-metric Indicators/Insights history, not price data) and
> `@docs/architecture/data-flow.md`'s "Per-item freshness"/"Live polling" sections, which this
> expands on. Verified against source (both this repo and `market-pulse-backend`) on 2026-09-14.

## Two separate pipelines, not one

"The chart" on Stock Detail / Asset Detail is actually two independent systems stitched together
by range selection, and they don't share caching logic:

- **Daily-close series — `ChartsRepository`/`ChartEntity`/`market_charts`.** One point per trading
  day, backs every range *except* 1D (5D/1M/6M/YTD/1Y). Cached in Room, one row per
  `(symbol, rangeKey)`.
- **Intraday bars — `IntradayRepository`/`market_intraday`.** Backs 1D specifically, plus the
  `SparklineChart` used on `StockPreviewCard`/dashboard tiles. **Never cached in Room** — deliberately
  in-memory only (`IntradayRepository`'s own doc comment explains why: the backend doc resets, not
  appends, on a new trading day, so persisting it locally would be actively wrong). Backed by a
  continuous poll loop, not an on-demand fetch.

`ChartRange.ONE_DAY` has no `/charts/:symbol` fetch at all — `StockDetailViewModel`/
`AssetDetailViewModel`'s `fetchChart`/`selectChartRange` explicitly skip it. If you're chasing a 1D
chart bug, you're in `IntradayRepositoryImpl`, not `ChartsRepositoryImpl`.

## `ChartRange` → API mapping (exact, not approximate)

From `ChartModels.kt`, confirmed against the backend's `fetchChartSeries` handler
(`marketPulse.ts`):

| Range | Client sends | Backend does |
|---|---|---|
| 1D | *(no call)* | n/a — served by `IntradayRepository` instead |
| 5D | `days=5` | `allCloses.slice(-5)` |
| 1M | `days=21` | `allCloses.slice(-21)` (≈ trading days/month) |
| 6M | `days=126` | `allCloses.slice(-126)` |
| YTD | `range=ytd` | filters `daily_closes` by ET date ≥ Jan 1 this year — the *one* calendar-based path |
| 1Y | `days=252` | `allCloses.slice(-252)` |

**`days=N` is a trading-day *bar count*, not a calendar-day span.** "6M" is not "the close from 6
calendar months ago" — it's "the last 126 stored trading-day closes," whatever calendar span that
covers once holidays/gaps are accounted for. A prior bug (fixed 2026-08-25, see `ChartModels.kt`'s
own doc comment) sent calendar-day counts (30/180/365) as if they were trading-day counts — visibly
wrong windows on-device. If a backend-computed figure (e.g. a `returns.m6` field) is computed via
actual calendar-date subtraction instead of this same trading-day-bar-count convention, it **will**
disagree with what the chart shows for the same nominal period — this exact mismatch is why
`technicalIndicators.returns` briefly needed reconciling with the chart's own definition.

There is no `THREE_MONTH` `ChartRange` — 3M shown anywhere in this app (e.g. the Returns card) is
backend-computed, with no client-side chart to cross-check it against.

## Room caching (daily-close series only)

- `ChartEntity` — one row per `(symbol, rangeKey)` composite key (`storage/database/entity/ChartEntity.kt`).
- No TTL at the DAO level (same as every other domain) — the staleness decision lives in
  `ChartsRepositoryImpl.refreshChart`, one layer up.
- `getChartStream` always reads Room; `refreshChart` decides whether to hit the network first.

## Freshness — `ChartSyncGroup` + `SyncManager.chartSyncTimestamps`

`ChartsRepositoryImpl.refreshChart(symbol, range, force, chartSyncGroup)` skips the network call
when the cached row's own `lastSyncedTimestamp` is already at or past the relevant flag's current
value, read from `SyncManager.chartSyncTimestamps: StateFlow<Map<String, Long>>` (populated off the
*same* `system/sync_status` listener `SyncManager` already runs — no second listener).

**Why 5 flags, not 1 or 1-per-symbol:**

- One global flag would fire on literally every symbol's routine write across the whole app —
  useless, since a cache check against it would never read as "fresh."
- One flag per *symbol* would need a per-symbol backend field, which `market_charts`/
  `market_intraday` don't have (and can't cheaply get — see the reverted approaches below), and
  Firestore rules don't let the client read those collections directly to check anyway.
- **The right granularity is "one flag per backend batch that writes `market_charts` together"** —
  each engine snapshots a whole group of symbols in one run, so a flag per group is both accurate
  (bumps exactly when that group's data actually changed) and cheap (no new per-symbol backend
  field).

| Flag | Bumped by | Covers |
|---|---|---|
| `charts_stocks_updated` | `stockAnalysisHub.ts`'s `checkCompletion()` | every individually-tracked stock (EOD fan-out) |
| `charts_equity_sector_updated` | `dashboardEngine.ts` | dashboard equity/sector tiles (SPY, XLK, ...) |
| `charts_sentiment_updated` | `dashboardEngine.ts` | FEAR_GREED/PUT_CALL |
| `charts_futures_commodities_updated` | `dashboardEngine.ts` | gold/silver/oil/copper, ES/NQ/YM |
| `charts_crypto_updated` | `dashboardEngine.ts` | BTC/ETH |

**Why `ChartSyncGroup` is caller-supplied, not derived from `AssetType`:** an individually-tracked
stock and a dashboard equity/sector tile can both be `AssetType.EQUITY`-ish, but they're written by
two different backend jobs (`stockAnalysisHub.ts` vs. `dashboardEngine.ts`) on two different
schedules — so `StockDetailViewModel`/`StockAnalysisViewModel` always pass `ChartSyncGroup.STOCKS`
explicitly; `AssetDetailViewModel` maps the asset's real `AssetOverview.type` via
`ChartSyncGroup.fromAssetType()`. `UNKNOWN` maps to `null`, which `ChartsRepositoryImpl` treats as
always-fetch rather than guessing.

**A flag absent from the map must read as "not fresh," never as timestamp `0`** — `0` would look
older than any real cached timestamp and wrongly suppress every refetch until the flag first fires
(e.g. right after this flag was newly deployed). `ChartsRepositoryImpl.isCacheFreshEnough` and both
history repos below use `?: return false` for exactly this reason, never `?: 0L`.

**Indicators'/Posture's/Positioning's per-metric history repos use the same mechanism**, added the
same day (`indicator_charts_updated`, `posture_charts_updated`, `positioning_charts_updated`) —
see `@docs/architecture/history-charts.md`'s gotchas list. Indicators needs no per-call
classification (one flag for all 5 pillars); Posture/Positioning resolve their flag internally via
the pre-existing `InsightsHistoryPillar.forMetricId()` lookup (bare ids like `"dark_pool_index"` —
**not** prefixed `"posture."`/`"positioning."`, that prefix only exists on the unrelated glossary-entry
id space `GlossaryDetailViewModel.labelResFor` uses).

## Intraday polling — a third, different shape

`IntradayRepository` isn't sync-flag-driven — intraday bars change *continuously* during market
hours (every 5 min for stocks via `intradayPoller.ts`, every 1 min for dashboard assets via
`dashboardEngine.ts`'s `refreshLiveDashboardPrices`), not once per day in one batch. A flag would
have to fire on nearly every poll tick to be useful, which is no better than a timer.

Two mechanisms, both on `trackSymbol(symbol, assetType, pollIntervalMs)`:

**Market-hours gating** (`IntradayRepositoryImpl.isMarketOpenFor`) — skips the actual network call
per poll tick while that asset class's market is closed:

| `AssetType` | Schedule | Source |
|---|---|---|
| EQUITY, SECTOR, INDEX | Mon-Fri 9:30am-4:00pm ET | client-computed (see caveat below) |
| FUTURE, COMMODITY | Sun 6pm ET → Fri 5pm ET (CME Globex); Mon-Thu 5-6pm ET daily halt **not** modeled, too short to bother | client-computed |
| CRYPTO | always open, 24/7 | no check |
| SENTIMENT, UNKNOWN | always poll — no well-defined closed window worth risking a missed update over | no check |

**Per-cadence poll interval** — `IntradayRepository.STOCK_POLL_INTERVAL_MS` (5 min, matches
`intradayPoller.ts`) is the default for stock-tracking callers; `DASHBOARD_POLL_INTERVAL_MS` (1
min, matches `dashboardEngine.ts`) is explicit for `DashboardViewModel`/`AssetDetailViewModel`.
Same "can't derive from `AssetType` alone" reasoning as `ChartSyncGroup` — stocks and dashboard
equity tiles share `AssetType.EQUITY` but poll at different rates.

**The cold-start exception, easy to get wrong again:** the gate must never skip the *very first*
fetch for a symbol this process lifetime, checked via `_seriesMap.value.containsKey(symbol)` —
**not** `_seriesMap.value[symbol] != null`, because a confirmed "no feed ever" 404 stores an
explicit `null` value that must still count as "already tried." Skipping that first fetch would
mean a cold app launch during closed hours never hydrates the last completed session's bars at
all — see "Bugs hit and dead ends" below, this was a real regression once already.

## Bugs hit and dead ends

- **First cutoff attempt (client-computed UTC midnight) was unsafe for one direction, wasteful in
  the other.** Tried making `ChartsRepositoryImpl` skip refetches based on "has the cached row been
  synced since the most recent UTC midnight" (safe for every asset class since UTC midnight is
  later than every class's real EOD snapshot time). Traced through concretely: a cache fetched at
  2pm ET (before that day's ~4:25pm stock EOD) still reads as "fresh" at 6pm ET (after EOD) because
  the UTC-midnight boundary (~7-8pm ET) hasn't passed yet — serving a stale, one-session-behind
  chart for that multi-hour window, every single day. Replaced entirely by the sync-flag mechanism
  above once it became available; don't reintroduce a wall-clock cutoff for this repository.
- **Adding a `timestamp` field to `/charts/:symbol`'s response, derived from `last_updated`, was a
  dead end.** By the time the client can read a per-document timestamp, it's already paid for the
  full response and the Firestore read behind it — checking freshness that way costs exactly what
  fetching costs. Tried, then reverted on the backend (`CHARTS.md §12`) in favor of the sync-flag
  approach; see `@docs/architecture/cross-repo-contracts.md` for the parallel note on
  `market_intraday`'s identical `last_updated`-as-raw-Firestore-Timestamp shape.
- **Market-hours gating regressed the "freeze last session until reopen" UX once, via the poll
  loop's own cold-start case** — see "the cold-start exception" above. The fix landed the same day;
  flagged here because it's exactly the kind of thing a future refactor of `trackSymbol`'s loop
  could reintroduce without realizing it.
- **Crude oil does not have its own schedule, despite looking like it might.** Verified directly in
  `dashboardEngine.ts`'s `getMarketStatus()`: gold, silver, crude oil, and the equity-index futures
  (ES/NQ/YM) are explicitly confirmed (comment dated 2026-08-24, checked against CME's published
  hours) to share one `isFuturesOpen` schedule. Don't split them out client-side without a matching
  backend split first.
- **`intradayPoller.ts`'s 5-min stock cadence and `dashboardEngine.ts`'s 1-min dashboard cadence are
  a real, not cosmetic, difference** — polling faster than the backend can produce a new bar just
  re-fetches identical data. This is why `pollIntervalMs` exists as a caller-supplied value instead
  of a single constant.
