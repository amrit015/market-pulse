# core/posture

**Purpose:** the market posture signal shown on the Insights screen.

- **Screen / ViewModel:** rendered inside `ui/screens/insights/views/InsightsRoute.kt`, read by `InsightsViewModel.kt`; history detail opens `ui/screens/insights/glossary/GlossaryDetailRoute.kt` (`GlossaryDetailViewModel`).
- **Repository:** `MarketPostureRepository` / `MarketPostureRepositoryImpl`.
- **Backend dependency:** Retrofit `insights/posture` for the current value and `insights/posture/history` for history.

`MarketPostureRepository` covers only the current posture: a Room-backed stream, with `refreshPosture(force)` driven by `SyncManager` on `market_posture_updated`. The history endpoint is not read here; it goes through `InsightsHistoryRepository` in `core/insights` (using `InsightsHistoryPillar`) and is gated on the `posture_charts_updated` flag. See `docs/architecture/history-charts.md` for the chart side.
