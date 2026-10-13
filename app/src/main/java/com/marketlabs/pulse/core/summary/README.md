# core/summary

**Purpose:** the AI-generated daily market briefing, with a calendar strip for browsing past days. Each day is split into three tabs.

- **Screen / ViewModel:** `ui/screens/summary/views/SummaryRoute.kt`, `SummaryScreen.kt`, `SummaryCalendarStrip.kt`, `SummaryViewModel.kt` (route `MARKET_SUMMARY`).
- **Repository:** `SummaryRepository` / `SummaryRepositoryImpl` (with `SummaryDateEntry`), plus `SummaryPreferencesRepository` (the persisted expanded/collapsed default).
- **Backend dependency:** Retrofit `pulse/v3/latest` and `pulse/v3/{dateId}` (`MarketPulseApi`).

Today's report uses the usual Room stream plus `refreshMarketSummary()`, driven by `SyncManager` (`market_pulse_updated`). Past dates go through `getMarketPulseForDate()` and `syncPastDate()`, which apply a caching rule anchored to New York time: fetch once, refetch once if the cached copy was written on the same day as the requested date, then treat it as final. A 404 writes a tombstone so that date is never queried again. `market_position`, `whatChanged` and `whatsNew` are always composed live by the backend, so the screen only renders them for today.

## Tabs and what expands

The day is chosen only by tapping the calendar strip; horizontal swipes move between tabs. The tab and the calendar strip are pinned; changing day keeps the tab.

| Tab | Cards, in order |
|---|---|
| Pulse | Market Signal (with Market Read), Market Sentiment, Market Position, What's New |
| Drivers | Market Movers, Macro Mix |
| Outlook | Domino Effect, What to Watch, Risks to Market |

- **Market Signal:** pills, signal line and conviction always visible; tapping the card expands the Market Read (analysis) and Where Capital's Moving (posture) inside it, between the headline and the conviction meter, which stays last. Conviction shows only the LOW / MODERATE / HIGH level beside its label (the backend's `conviction_reason` sentence is not displayed). There is no separate Read card or sheet.
- **Market Sentiment:** three-line preview, tap to expand (unchanged).
- **Market Movers / Macro Mix:** collapsed is the headline (and tag, for Macro Mix) only, no preview lines, so a paid tier can lock the summaries without the card changing shape. Each row's tap overrides the screen-level **Expand all / Collapse all** default (shown only on the Drivers tab), which is persisted (`SummaryPreferencesRepository`, default collapsed). That default does not touch the Market Read or Market Sentiment.
- **Position and What's New** render only for the latest day with a report, because the backend composes them live.
- **Resets:** changing day recreates `SummaryExpansionState`, collapsing everything again; changing the default clears row overrides only.
- **Entitlement:** every interaction a paid tier will lock goes through `SummaryAccess.allows(SummaryFeature)`, fed by `EntitlementRepository.isPro` (always `true` in v1).

Analytics: one `screen_view` per tab (`summary_pulse` / `summary_drivers` / `summary_outlook`, not re-fired by a day change), `section_expand`, `summary_expand_mode`, `day_select`. See `docs/architecture/analytics.md`.
