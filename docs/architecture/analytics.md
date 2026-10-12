# Analytics & Crashlytics

**Status:** written 2026-10-02 alongside the implementation. Verified against source at that date.

All Firebase Analytics calls go through `core/analytics/AnalyticsTracker`; only
`FirebaseAnalyticsTracker` imports Firebase. Event names live in `AnalyticsEvent` and must never
be renamed once shipped (some are A/B goal events).

## Screens
- `TrackScreen(target, vararg keys, params)` (`ui/common/TrackScreen.kt`) logs `screen_view` on
  resume and whenever `keys` change (the selected tab). It filters the re-resume that follows a
  rotation.
- Non-tabbed routes call it from their `composable(...)` block in `PulseNavGraph.kt`; the four tabbed
  Routes (Indicators, Insights, Analysis, Stock Detail) call it themselves with
  `ScreenName.X.forTab(tab)`.
- `ScreenName` is a fixed enum, never derived from the resolved route, so URLs and glossary free
  text can't leak. `ScreenCatalogTest` fails when a `PulseRoutes` constant has no `ScreenName`.

## Events and user properties
- Params are `String`/`Long` only, no free text or URLs (`AnalyticsEventContractTest` enforces the
  Firebase limits).
- `AnalyticsStateSync` (started in `PulseApplication`) keeps collection-enabled and all seven user
  properties in step with app state; features don't set properties themselves.
- Shared components (bottom sheets, error/empty states) read `tracker.currentScreen` instead of
  taking a screen name from every caller.

## Opt-out and debug builds
- "Share anonymous usage data" (Settings → Data & Sync) is backed by `AnalyticsConsentRepository`,
  default on. Effective collection = `AnalyticsCollection.effective(userEnabled)`, which also
  requires a non-debug build or `BuildConfig.ANALYTICS_IN_DEBUG` (never commit `true`).
- The manifest sets `firebase_analytics_collection_enabled=false` so nothing is sent before the
  stored choice has loaded.
- Crashlytics is not governed by the toggle; it is disabled in debug builds.

## Verifying
Flip `ANALYTICS_IN_DEBUG` locally, run `adb shell setprop debug.firebase.analytics.app com.marketlabs.pulse`,
and watch Firebase DebugView.
