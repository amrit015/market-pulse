# Per-metric history charts

The shared chart infrastructure behind every "tap a metric, see its history" page: Indicators'
`MetricDetailScreen` (26 metrics, 4 pillars) and Posture/Positioning's `GlossaryDetailScreen` (14
metrics, 2 pillars). Built for Indicators first, then extended to Posture/Positioning reusing the
same chart component and range-picker logic — this doc exists so a third domain doesn't
re-duplicate either. **Status:** added 2026-09-08/09, LIVING doc — extend it when a real bug or a
new domain changes something here, don't let it drift.

## The shared pieces (domain-agnostic)

- **`ui/components/charts/IndicatorHistoryChart.kt`** — the actual chart. Despite the name (kept
  from when it was Indicators-only; not worth a rename), it's generic: takes `points: List<MetricHistoryPoint>`
  and a plain `isStepLine: Boolean` the *caller* decides, not a `metricId` with a lookup baked in.
  Handles empty (0 points), single-point (1 point — can't draw a line, shows a centered
  value+date marker instead), and multi-point series; step-after vs. smoothed-cubic line via a
  custom Vico `PointConnector` (Vico only ships `Sharp`/`cubic()` built in).
- **`core/charts/HistoryChartRangeFiltering.kt`** — the range-picker engine
  (`computeAvailableChartRanges`/`filteredForRange`/`resolveEffectiveRange`), generic over point
  type via a `dateOf: (T) -> String` selector so it works for both domains' point shapes without
  either one being coerced into the other's. Reuses `ChartRange`/`ChartRangePicker` (originally
  built for price charts on Stock/Asset Detail), but **not** their `isCoveredByHistory` check —
  that only asks "does history reach back this far," which is the wrong question for a series
  that isn't one point per calendar day. Instead it filters by each point's own real `date` and
  only keeps a range if it shows a *different, non-degenerate* (≥2 points) slice than the next
  narrower one already kept — a macro metric with 12 points spread one-a-month across a year drops
  "5D"/"1M" (both round to 0-1 points) but keeps "6M"/"YTD"/"1Y". A metric that can't clear that bar
  for more than one range (a single point, or every point crammed into a few days) hides the picker
  entirely rather than showing a control with nothing to pick between.
- **`ui/components/charts/PeriodChartMarker.kt`** — shared touch-marker balloon. Every chart using
  it (price charts, both history-chart domains) renders the same 2-line shape: line 1 is a date
  (full date with year for daily/history charts, date + local time for the 1D intraday price
  chart), line 2 is the value + percent change together.

## Where the two domains differ

| | Indicators (`MetricDetailScreen`) | Posture/Positioning (`GlossaryDetailScreen`) |
|---|---|---|
| Metric ids | 26, across 4 pillars | 14, across 2 pillars |
| Id/cadence table | `core/indicators/MetricHistoryPillar` | `core/insights/InsightsHistoryPillar` |
| Endpoints | `indicators/{tactical,valuation,vitals}/history`, `risk/history` (one shared `IndicatorsApi`) | `insights/{posture,positioning}/history` (two separate `Api` interfaces — `RemoteInsightsHistoryDataSourceImpl` routes across both) |
| Point shape | `{date, value, value_display, signal_color}` — backend pre-formats | `{date, value, status}` — **no** pre-formatted string or color; client formats `value` (see below) |
| Domain model | `MetricHistoryPoint`/`MetricHistorySeries` | `InsightsHistoryPoint`/`InsightsHistorySeries` — deliberately a separate, near-identical model, not reused across domains (matches this app's vertical-slicing convention; only the *chart component* and *range-filtering engine* are actually shared) |
| Room table | `metric_history` | `insights_history` |
| Entry point | dedicated push destination, one metric per screen | reused existing multi-glossary-id card page; a `chartMetricId` nav arg (added alongside the existing `metricIds` glossary ids) says which one chart to show — a COT/short-interest row already covers 2-3 glossary ids but always maps to exactly one chart |
| Step-line set | `MetricHistoryPillar.isMacroCadence` — 8 monthly/quarterly macro metrics | `InsightsHistoryPillar.isSparseCadence` — everything except `dark_pool_index`/`net_liquidity` (12 of 14; almost everything here is weekly/bi-monthly/irregular) |
| Value formatting | none needed — `valueDisplay` comes from the backend | `GlossaryDetailScreen.formatInsightsValue` — mirrors each metric's own **live card** formatting exactly (percent, `$X.XXT`, share-count K/M suffix, pts), so a historical chart point reads identically to today's reading on the card above it |
| Status color | N/A (`signal_color` unused in rendering even for Indicators) | plain text only, by product decision — `signalColor` is always `SignalColor.UNKNOWN` when adapting `InsightsHistoryPoint` → `MetricHistoryPoint` for the chart |

## Default chart range, per screen (product decision, not spec-driven)

- **Overview** (`AssetDetailViewModel`) — 1D for live-feed assets (equities/crypto/commodities/
  sector ETFs, i.e. `DashboardIntradayEligibility.isEligible`), 1M for the rest (VIX/Fear & Greed/
  Put-Call — "Sentiment and Fear," no live feed so 1D isn't even offered).
- **Market Analysis** (`StockDetailViewModel`) — 1M for every symbol.
- **Indicators** and **Posture/Positioning** — 1M for every metric. Only ever a *preferred*
  starting point: `resolveEffectiveRange` clamps to the widest range that's actually available
  whenever 1M isn't meaningful for that specific metric's data (most of the sparse-cadence ones).

## Gotchas actually hit building this

- **`remember`'s calculation lambda disallows composable calls.** `GlossaryDetailScreen`'s
  `formatInsightsValue` needs `stringResource` (for the shares K/M suffix), so the
  `InsightsHistoryPoint → MetricHistoryPoint` mapping is a plain `.map {}` directly in the
  composable body, not wrapped in `remember {}` — `remember`'s calculation param is
  `@DisallowComposableCalls`. Fine perf-wise at ≤180 points.
- **A capped/flat backend fetch (no `days`/`range` query param) means range switching is 100%
  client-side.** Both `MetricDetailViewModel` and `GlossaryDetailViewModel` fetch once
  (`limit=180`, the backend's hard cap) in `onStart()` and re-slice that one cached series on every
  range tap — no network call, unlike `StockDetailViewModel`/`AssetDetailViewModel`'s price charts,
  which *do* refetch per range since `market_charts` really does cache one row per range.
- **Point-count-based slicing (`takeLast(range.days)`) is wrong for sparse data.** The backend's
  `ChartRange.days` field means "trading-day point count" for `market_charts` (one point per
  trading day), not a calendar-day cutoff — the first version of the range picker here used it
  directly and every range button above the metric's own point count silently rendered the exact
  same "1M" slice as "1Y." Fixed by filtering on each point's real `date` instead (see
  `HistoryChartRangeFiltering.kt` above).
- **`AssetDetailViewModel.onStart()`'s chart fetch needs an `ONE_DAY` guard.** Making 1D a possible
  *initial* default (not just something tapped into) surfaced that `onStart()` unconditionally
  fetched a `/charts/:symbol` chart for whatever range was current — harmless before since the old
  FIVE_DAY default never hit that branch on first load, but `ChartRange.ONE_DAY` has no such fetch
  to make (only `selectChartRange` guarded against it).
