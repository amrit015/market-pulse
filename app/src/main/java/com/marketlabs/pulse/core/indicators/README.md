# core/indicators

**Purpose:** macro indicators across four pillars (tactical momentum, systemic risk, valuation, macro vitals), plus on-demand per-metric history.

- **Screens / ViewModels:** `ui/screens/indicators/views/IndicatorsRoute.kt` and `IndicatorsViewModel.kt`; `IndicatorHorizonsRoute`; `ui/screens/indicators/detail/MetricDetailRoute.kt` and `MetricDetailViewModel.kt`.
- **Repositories:** `IndicatorsRepository` / `IndicatorsRepositoryImpl`, `MetricHistoryRepository` / `MetricHistoryRepositoryImpl` (with `MetricHistoryPillar`).
- **Backend dependency:** Retrofit `indicators/synthesis`, `indicators/tactical`, `risk/latest`, `indicators/valuation`, `indicators/vitals`, and the matching `*/history` endpoints.

`RemoteIndicatorsDataSourceImpl` fetches the synthesis and pillar endpoints in parallel and combines them into one `MarketIndicators`, which is cached in Room and refreshed by `SyncManager` (`master_ingestion_updated`, `indicator_synthesis_updated`). `MetricHistoryRepository` is separate and on-demand per `metricId`; it is gated on the `indicator_charts_updated` flag held in `SyncManager.chartSyncTimestamps`. Chart behaviour is documented in `docs/architecture/history-charts.md` and the screen layout in `collapsing-header-tabs.md`.
