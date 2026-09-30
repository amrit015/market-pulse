# core/summary

**Purpose:** the AI-generated daily market briefing, with a calendar strip for browsing past days.

- **Screen / ViewModel:** `ui/screens/summary/views/SummaryRoute.kt`, `SummaryScreen.kt`, `SummaryCalendarStrip.kt`, `SummaryViewModel.kt` (route `MARKET_SUMMARY`).
- **Repository:** `SummaryRepository` / `SummaryRepositoryImpl` (with `SummaryDateEntry`).
- **Backend dependency:** Retrofit `pulse/v3/latest` and `pulse/v3/{dateId}` (`MarketPulseApi`).

Today's report uses the usual Room stream plus `refreshMarketSummary()`, driven by `SyncManager` (`market_pulse_updated`). Past dates go through `getMarketPulseForDate()` and `syncPastDate()`, which apply a caching rule anchored to New York time: fetch once, refetch once if the cached copy was written on the same day as the requested date, then treat it as final. A 404 writes a tombstone so that date is never queried again. `market_position`, `whatChanged` and `whatsNew` are always composed live by the backend, so the screen only renders them for today.
