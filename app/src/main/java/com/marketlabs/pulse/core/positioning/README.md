# core/positioning

**Purpose:** the market positioning signal shown on the Insights screen.

- **Screen / ViewModel:** rendered inside `ui/screens/insights/views/InsightsRoute.kt`, read by `InsightsViewModel.kt`; history detail opens `ui/screens/insights/glossary/GlossaryDetailRoute.kt` (`GlossaryDetailViewModel`).
- **Repository:** `MarketPositioningRepository` / `MarketPositioningRepositoryImpl`.
- **Backend dependency:** Retrofit `insights/positioning` for the current value and `insights/positioning/history` for history.

It mirrors `core/posture`: the repository handles the current value only, as a Room-backed stream refreshed by `SyncManager` on `market_positioning_updated`. History is read through `InsightsHistoryRepository` in `core/insights` and gated on the `positioning_charts_updated` flag. Chart behaviour is in `docs/architecture/history-charts.md`.
