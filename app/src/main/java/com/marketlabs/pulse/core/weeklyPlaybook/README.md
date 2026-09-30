# core/weeklyPlaybook

**Purpose:** the weekly playbook of upcoming market events and scenarios on the Insights screen.

- **Screen / ViewModel:** `ui/screens/insights/views/WeeklyPlaybookView.kt` inside `InsightsRoute.kt`, read by `InsightsViewModel.kt`.
- **Repository:** `WeeklyPlaybookRepository` / `WeeklyPlaybookRepositoryImpl`.
- **Backend dependency:** Retrofit `dashboard/playbook` (`WeeklyPlaybookApi`). Despite the `dashboard/` path prefix, this is not part of `core/dashboard`.

The repository exposes a Room-backed `getPlaybookStream()` with no cache expiration; `refreshPlaybook(force)` is driven by `SyncManager`. `SyncManager` watches two flags for this domain, `weekly_playbook_updated` and `weekly_playbook_actuals_updated`, the second covering actual values filling in after releases. `core/pastReleases` supplies the historical counterpart shown alongside it.
