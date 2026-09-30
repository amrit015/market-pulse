# core/marketRisk

**Purpose:** the tail-risk assessment shown on the Insights screen.

- **Screen / ViewModel:** rendered inside `ui/screens/insights/views/InsightsRoute.kt` and read by `ui/screens/insights/InsightsViewModel.kt` (shared with posture, positioning, weeklyPlaybook and pastReleases).
- **Repository:** `MarketRiskRepository` / `MarketRiskRepositoryImpl`.
- **Backend dependency:** Retrofit `risk/tail-risks` (`MarketRiskApi`).

The repository exposes a Room-backed `getTailRisksStream()` and a `refreshTailRisks(force)` that is driven by `SyncManager` on the `market_risks_updated` flag rather than by any cache expiry. Its timestamp methods carry a `TailRisks` suffix (`getLastSyncedTimestampTailRisks`), unlike the other Insights repositories. It is unrelated to the `risk/latest` and `risk/history` endpoints, which belong to the systemic-risk pillar in `core/indicators`.
