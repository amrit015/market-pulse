# Analytics & Crashlytics

**Status:** written 2026-10-02 alongside the implementation. Verified against source at that date.

All Firebase Analytics calls go through `core/analytics/AnalyticsTracker`; only
`FirebaseAnalyticsTracker` imports Firebase. Event names live in `AnalyticsEvent` and must never
be renamed once shipped (some are A/B goal events).

## Screens
- `TrackScreen(target, vararg keys, params)` (`ui/common/TrackScreen.kt`) logs `screen_view` on
  resume and whenever `keys` change (the selected tab). It filters the re-resume that follows a
  rotation.
- Non-tabbed routes call it from their `composable(...)` block in `PulseNavGraph.kt`; the five tabbed
  Routes (Indicators, Insights, Analysis, Stock Detail, Summary) call it themselves with
  `ScreenName.X.forTab(tab)`. Summary reports `summary_pulse` / `summary_drivers` /
  `summary_outlook`; its calendar-strip day change is not part of the key, so it is not a new view.
- `ScreenName` is a fixed enum, never derived from the resolved route, so URLs and glossary free
  text can't leak. `ScreenCatalogTest` fails when a `PulseRoutes` constant has no `ScreenName`.

## Events and user properties
- Params are `String`/`Long` only, no free text or URLs (`AnalyticsEventContractTest` enforces the
  Firebase limits).
- `AnalyticsStateSync` (started in `PulseApplication`) keeps collection-enabled and all seven user
  properties in step with app state; features don't set properties themselves.
- Shared components (bottom sheets, error/empty states) read `tracker.currentScreen` instead of
  taking a screen name from every caller.

## Summary events
- `section_expand` -- a hidden part was expanded (never on collapse). Params: `screen_name` (the
  tab's screen name), `section` (`market_movers` | `macro_mix` | `market_sentiment` | `market_read` |
  `what_changed`), `item_position` (index within the section, 0 for single-item sections),
  `day_offset` (days behind the latest report, 0 = latest -- anchored to the latest day that
  actually has a report, not to today). No headline text is ever sent.
- `summary_expand_mode` -- the user flipped the Expand all / Collapse all default. Param `mode`:
  `expanded` | `collapsed`.
- `day_select` -- a day was tapped in the calendar strip. Param `day_offset`, same meaning as above.
- The `market_read` value is gone from `sheet_open`'s `sheet` param: the Market Read is now an
  inline expansion of the Signal card, not a sheet. It never shipped, so nothing was renamed.

## The anonymous user ID
Deliberately not used as the Analytics user ID or the Crashlytics user ID. It is shown to the user
in Settings → About for support/deletion requests and sent as a bearer token to the backend, and
nothing else (see `@docs/architecture/auth.md`). The "Share anonymous usage data" toggle does not
control it; the privacy policy says so.

## Opt-out and debug builds
- "Share anonymous usage data" (Settings → Data & Sync) is backed by `AnalyticsConsentRepository`,
  default on. Effective collection = `AnalyticsCollection.effective(userEnabled)`, which also
  requires a non-debug build or `BuildConfig.ANALYTICS_IN_DEBUG` (never commit `true`).
- The manifest sets `firebase_analytics_collection_enabled=false` so nothing is sent before the
  stored choice has loaded.
- Crashlytics is not governed by the toggle; it is disabled in debug builds.

## Privacy policy and legal version
The Privacy Policy (`assets/legal_privacy.json`) discloses Analytics, Crashlytics, App Check and
AdMob, and says analytics can be turned off in Settings → Data & Sync. Changing what is collected
means updating that JSON (and the `data_sync_section_*` strings on the Data & Sync screen) in the
same release and bumping `LegalRepository.CURRENT_LEGAL_VERSION`, which sends existing users back
through acceptance.

## Verifying
Flip `ANALYTICS_IN_DEBUG` locally, run `adb shell setprop debug.firebase.analytics.app com.marketlabs.pulse`,
and watch Firebase DebugView.
