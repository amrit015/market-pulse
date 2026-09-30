# Feature index — `core/<domain>` folders

One row per `app/src/main/java/com/marketlabs/pulse/core/<domain>/` folder. Read the per-feature
`README.md` inside the folder for the feature rows; the infra rows point at their existing
architecture docs instead. Verified against source 2026-09-30.

**Feature** = owns a user-facing screen (or a tab on one) and a data-driven or content-driven
domain. **Infra** = shared, cross-cutting, no screen of its own (comparable to the backend's
`utils/`).

Transport shorthand: *Retrofit* = the shared `MarketPulseRetrofit` against the Express API;
*Firestore* = a direct client SDK read. See `@docs/architecture/data-flow.md`.

## Features

| Name | Kind | Purpose | Primary screen / route | Backend endpoint / data source | Existing doc coverage |
|---|---|---|---|---|---|
| [dashboard](../app/src/main/java/com/marketlabs/pulse/core/dashboard/README.md) | Feature | Live market overview tiles (indices, sectors, assets) | `DashboardRoute` (`MARKET_OVERVIEW`), `AssetDetailRoute` | Firestore `market_overview` snapshot listener (no REST) | `data-flow.md` §B (as the direct-Firestore example only) |
| [indicators](../app/src/main/java/com/marketlabs/pulse/core/indicators/README.md) | Feature | Macro indicators across four pillars, plus per-metric history | `IndicatorsRoute` (`MARKET_INDICATORS`), `MetricDetailRoute`, `IndicatorHorizonsRoute` | Retrofit `indicators/{synthesis,tactical,valuation,vitals}`, `risk/latest`, `*/history` | `history-charts.md` (history), `collapsing-header-tabs.md` (screen pattern) |
| [news](../app/src/main/java/com/marketlabs/pulse/core/news/README.md) | Feature | Curated news feed | `NewsRoute` (`MARKET_NEWS`) | Retrofit `news/latest`, `news/history` | none |
| [summary](../app/src/main/java/com/marketlabs/pulse/core/summary/README.md) | Feature | AI-generated daily market briefing with a past-date strip | `SummaryRoute` (`MARKET_SUMMARY`) | Retrofit `pulse/v3/latest`, `pulse/v3/{dateId}` | `collapsing-header-tabs.md` (pinned-zone variant), one line in `known-gaps.md` |
| [stocks](../app/src/main/java/com/marketlabs/pulse/core/stocks/README.md) | Feature | Per-stock analysis: list, detail, deep dive | `StockAnalysisRoute` (`MARKET_ANALYSIS`), `StockDetailRoute`, `DeepDiveRoute` | Retrofit `stocks/previews`, `stocks/{symbol}/detail`, `stocks/{symbol}/detail/deep` | `collapsing-header-tabs.md` (Stock Detail), `data-flow.md` (worked example) |
| [learn](../app/src/main/java/com/marketlabs/pulse/core/learn/README.md) | Feature (static content) | Bundled tutorials: concepts, mechanism decks, indicator articles | `TutorialsHubScreen` (`TUTORIALS_HUB`) | none — local/static (`assets/learn_content.json`, `assets/indicator_articles.json`) | none (incidental mention in `collapsing-header-tabs.md`) |

### Insights group

`InsightsRoute` (`MARKET_INSIGHTS`) is one screen whose tabs render five sub-domains. All five are
read by the single `InsightsViewModel`, so they are listed together here rather than as five
separate screens. `core/insights` itself is not one of them: it holds only the shared
history-chart repository and pillar enum for Posture/Positioning detail screens (covered by
`history-charts.md`).

| Name | Kind | Purpose | Primary screen / route | Backend endpoint / data source | Existing doc coverage |
|---|---|---|---|---|---|
| [marketRisk](../app/src/main/java/com/marketlabs/pulse/core/marketRisk/README.md) | Feature | Tail-risk assessment | `InsightsRoute` tab | Retrofit `risk/tail-risks` | none |
| [posture](../app/src/main/java/com/marketlabs/pulse/core/posture/README.md) | Feature | Market posture signal | `InsightsRoute` tab; detail via `GlossaryDetailRoute` | Retrofit `insights/posture`, `insights/posture/history` | `history-charts.md` (history only) |
| [positioning](../app/src/main/java/com/marketlabs/pulse/core/positioning/README.md) | Feature | Market positioning signal | `InsightsRoute` tab; detail via `GlossaryDetailRoute` | Retrofit `insights/positioning`, `insights/positioning/history` | `history-charts.md` (history only) |
| [weeklyPlaybook](../app/src/main/java/com/marketlabs/pulse/core/weeklyPlaybook/README.md) | Feature | Weekly playbook of upcoming events | `WeeklyPlaybookView` inside `InsightsRoute` | Retrofit `dashboard/playbook` | none |
| [pastReleases](../app/src/main/java/com/marketlabs/pulse/core/pastReleases/README.md) | Feature | Past economic releases with actuals | `PastReleasesRoute` (`PAST_RELEASES`) | Retrofit `dashboard/past-releases` | none |

## Infra

No new READMEs; these keep their existing architecture-doc coverage.

| Name | Kind | Purpose | Primary screen / route | Backend endpoint / data source | Existing doc coverage |
|---|---|---|---|---|---|
| sync | Infra | `SyncManager`: one Firestore listener on `system/sync_status` that triggers repository refreshes | none (surfaced in `DataSyncScreen`) | Firestore `system/sync_status` | `data-flow.md`, `cross-repo-contracts.md` |
| charts | Infra | Price-history charts and sparklines; `ChartSyncGroup` freshness flags | none (used by Stock Detail, Dashboard, Metric Detail) | Retrofit `charts/{symbol}` | `price-charts.md`, `data-flow.md`, `cross-repo-contracts.md` |
| intraday | Infra | Intraday series with market-hours gating and per-cadence polling | none | Retrofit `intraday/{symbol}` | `price-charts.md`, `data-flow.md` |
| notifications | Infra | FCM topics, channels, tap routing | `NotificationsRoute` (settings) | FCM only | `push-notifications.md` |
| glossary | Infra | Bundled term/definition providers shared by several screens | none of its own | none — bundled JSON | `guidelines/compose-conventions.md` only; no architecture doc |
| ads | Infra | `AdManager`: MobileAds init, ad-free state, native ad loading | none (consumed by `NewsViewModel`) | Google Mobile Ads SDK | none |
