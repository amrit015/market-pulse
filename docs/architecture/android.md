# Android presentation layer

ViewModel/UiState shape, the Route/Screen split, and how navigation is wired — the parts of the
app that are Compose/Android-specific rather than domain-data-flow-specific (see
`@docs/architecture/data-flow.md` for that half).

**Status:** restructured 2026-08-31 from the prior root `ARCHITECTURE.md` §6. Content
re-verified against source during the move.

## ViewModel + UiState

Every `@HiltViewModel` follows the same shape: constructor-inject the domain `Repository` (+
`SyncManager`), hold private `MutableStateFlow`s for `isLoading`/`isRefreshing`/`errorMessage`,
`combine()` those with the repository's `Flow<Domain>` into one public
`val uiState: StateFlow<XUiState>` via
`.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = XUiState(isLoading = true))`.

`XUiState` is a flat `data class` (nullable/default fields) in every domain except `summary`,
which uses a sealed `Loading/Success/Error` interface — the one deliberate outlier, since that
screen genuinely renders different layouts per state rather than just toggling flags.

## Route / Screen split

Every screen is two composables:

- **`XRoute`** — stateful. Calls `hiltViewModel()`, `collectAsStateWithLifecycle()`, wires a
  `DisposableEffect(LocalLifecycleOwner) { LifecycleEventObserver { ON_START → viewModel.onStart(); ON_STOP → viewModel.onStop() } }`,
  owns the `PullToRefreshBox`/`SnackbarHost`, and — for screens that are pushed rather than
  tab-hosted (`stock_detail`-style pushes, `News`, `Settings`, `Indicator Horizons`) — its own
  `Scaffold`/`TopAppBar` with a back button.
- **`XScreen`** — stateless. Plain data + lambdas, no ViewModel awareness.

**Sub-pattern: a screen with its own internal tabs** (Stock Analysis detail, Insights). The Route
pins a shared `PulseTabRow` (`ui/components/PulseTabRow.kt`) above the pull-to-refresh area; the
ViewModel holds the selected index as `MutableStateFlow<Int>` folded into the UiState
(`selectedTabIndex: Int`, `onTabSelected(index)`); the Screen branches on a per-screen
`enum class XTab(val labelRes: Int)` and renders each tab as its own `LazyColumn` with its own
`LazyListState`, so scroll position survives switching tabs and back. Full styling convention in
`@docs/guidelines/compose-conventions.md`.

## Navigation

`ui/navigation/PulseNavGraph.kt` is the single `NavHost`. Two kinds of destinations:

- **Bottom-nav tabs** (`Overview`, `Indicators`, `Summary`, `Insights`, `Analysis`) — navigated
  to with the tab-preserving pattern
  (`popUpTo(startDestination) { saveState = true }; launchSingleTop = true; restoreState = true`),
  so switching tabs doesn't lose each tab's scroll position/back stack.
- **Pushed destinations** (`webview/{encodedUrl}`, `market_news`, `settings`,
  `indicator_horizons`, plus symbol/asset/metric/glossary detail routes) — plain
  `navController.navigate(route)`, popped with `navController.popBackStack()`. `News` used to be
  a bottom-nav tab; it's now reachable only by tapping into it from the Dashboard's news preview,
  which is why it grew its own `TopAppBar`. `indicator_horizons` (2026-08-22) started as local
  Compose state inside the Indicators tab's own screen and was promoted to a real destination
  specifically so `MainActivity`'s `isPushedDestination` check could exclude it from the global
  top bar and floating nav, which a same-composable-tree toggle has no way to do.

One-shot cross-screen signals (e.g. "scroll the News list to this specific article after a
Dashboard preview-card tap") are **not** passed as nav arguments — they're hoisted as plain
`remember { mutableStateOf(...) }` state inside `PulseNavGraph()` itself and consumed-then-nulled
by the destination, specifically so they don't disturb the bottom-nav tab's plain route-string
identity (a query-param route would break the `currentDestination.route == item.route`
selected-tab check).

**A contextual jump into another tab's content** (Summary's Drivers section → Indicators tab).
This isn't a real tab switch (the user didn't tap the bottom bar) but should land on the same
live Indicators instance a real tab switch would, and system back should return to Summary
specifically. Two things both have to be true:

- The navigation call uses the *identical* tab-switch `popUpTo`/`launchSingleTop`/`restoreState`
  block a real bottom-nav tap uses — mixing "plain push" and "restoreState tab switch" navigation
  to the same route confuses Navigation-Compose's saved-state bookkeeping, silently no-oping
  later taps on other tabs.
- The `BackHandler` that redirects back-to-Summary has to be composed *inside* the Indicators
  destination's own `composable(...)` content in `PulseNavGraph.kt`, not up in `MainActivity`.
  `NavHost` registers its own internal back handling as part of composing itself; a `BackHandler`
  composed *before* `NavHost` always loses to it (the dispatcher processes the most-recently-added
  enabled callback first). One composed as a child of `NavHost` is added after, and only then
  actually takes priority for that screen.

**Reporting screen-loaded data up to `MainActivity`'s chrome.** The global top bar is normally a
static per-route title, but Summary's report-type label ("Daily Update"/"Weekend Update") is only
known once that screen's own data loads. Rather than reach into `SummaryViewModel`'s state
directly, the value is reported *up*: `MarketSummaryRoute` fires a `LaunchedEffect`-driven
callback (`onReportTypeLoaded`) the moment its `Success` state carries a `ReportType`, threaded
through `PulseNavGraph`'s params to a `mutableStateOf<ReportType?>` held in `MainActivity`, which
`topBarTitle()` reads with a static-string fallback for the not-yet-loaded case. This is the
precedent to reuse for any future "top bar needs a value only some destination's data actually
has."

## Pushed screens must match the page background explicitly

`Scaffold`'s own `containerColor` defaults to `colorScheme.background`, but a plain `TopAppBar`
with no `colors` override defaults to `colorScheme.surface` — which no longer tracks `background`
now that light mode's background carries a per-preset accent tint (see
`@docs/theming-system/theming-spec.md`). Every screen that owns its own `Scaffold`/`TopAppBar`
(`SettingsScreen.kt`, `IndicatorHorizonsRoute.kt`, `WebViewScreen.kt`, `NewsRoute.kt`,
`AssetDetailRoute.kt`) passes
`colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)`
explicitly for this reason (fixed 2026-08-31) — a new pushed screen needs the same override or
its top bar reads as a visibly different-colored band.
