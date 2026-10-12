# core/pastReleases

**Purpose:** past economic releases, shown as the historical counterpart to the Weekly Playbook.

- **Screen / ViewModel:** `ui/screens/insights/views/PastReleasesRoute.kt` and `PastReleasesView.kt` (route `PAST_RELEASES`); the data is loaded by `ui/screens/insights/InsightsViewModel.kt`.
- **Repository:** `PastReleasesRepository` / `PastReleasesRepositoryImpl`.
- **Backend dependency:** Retrofit `dashboard/past-releases` (`PastReleasesApi`); like the playbook, the `dashboard/` prefix does not make it part of `core/dashboard`.

The repository exposes a Room-backed `getPastReleasesStream()`; `refreshPastReleases(force)` is driven by `SyncManager`, which detects the weekly `past_releases_updated` flag, with no cache expiry. It has its own route rather than being a tab, but shares `InsightsViewModel` with the rest of the Insights group.

## List behaviour

The Past Releases screen sorts releases alphabetically by label (case-insensitive, unlabelled last). Each card starts collapsed to its title, date, estimate and actual, with an arrow centered against that block (and no impact or divider below it); tapping the card opens the Post-Release Impact under a divider, and the Expand all / Collapse all button above the list sets every card at once (clearing individual taps). That default is persisted (`PastReleasesPreferencesRepository`, default collapsed) and is independent of Summary's own expand/collapse default; individual card taps are per visit.
