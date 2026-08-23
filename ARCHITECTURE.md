# MarketPulse — Architecture Report

A survey of how the Android app is built: the layers, how network calls are made, how data is cached, and how the presentation layer is wired together. Package root: `com.marketlabs.pulse`.

**For rules and conventions when writing new code, see `CLAUDE.md`.** This document explains *how the system works*, not *how to write in it*.

## Tech stack

Kotlin, Jetpack Compose (Material 3), Dagger Hilt, Room, Retrofit + Moshi, Firebase Firestore (client SDK for some domains, backing store for all of them), Compose Navigation.

## 1. The shape of a domain

Every feature ("domain") — `dashboard`, `indicators`, `news`, `marketRisk`, `posture`, `summary`, `weeklyPlaybook`, `stocks` — is vertically sliced across five package roots:

```
network/model/<domain>/      Network<X>.kt        — Retrofit/Moshi response DTOs
network/api/<Domain>Api.kt                        — Retrofit @GET interface
network/store/<domain>/      Remote<Domain>DataSource(Impl).kt
storage/model/<domain>/      Domain<X>.kt         — clean, UI-facing domain models
storage/model/<domain>/mappers/                   — Network→Domain, Domain↔Entity
storage/database/entity/     <Domain>Entity.kt    — Room table(s)
storage/database/dao/        <Domain>Dao.kt
storage/database/converters/ <Domain>Converters.kt — Moshi (de)serializers for nested JSON columns
storage/store/<domain>/      Local<Domain>DataSource(Impl).kt — wraps the DAO
core/<domain>/               <Domain>Repository(Impl).kt      — the ViewModel-facing façade
di/<Domain>Module.kt                              — Hilt @Provides bindings
ui/screens/<domain>/         <Domain>ViewModel.kt, <Domain>UiState.kt
ui/screens/<domain>/views/   <Domain>Route.kt, <Domain>Screen.kt
```

Data flows in one direction: **Remote → Local (Room) → Repository → ViewModel → Screen.**

```
Backend (Cloud Functions v2)          Android
┌─────────────────────┐               ┌──────────────────────────────────────────────────┐
│ Firestore documents │──writes──────▶│ market_stocks, market_overview, market_news, ...  │
└─────────────────────┘               └──────────────────────────────────────────────────┘
        │                                          │
        │ read via Express API                     │ read directly (client SDK)
        ▼                                          ▼
 RemoteXDataSourceImpl (Retrofit)         RemoteXDataSourceImpl (FirebaseFirestore)
        │                                          │
        └───────────────┬──────────────────────────┘
                        ▼
              LocalXDataSourceImpl (Room DAO)
                        ▼
                  XRepositoryImpl
                        ▼
                    XViewModel  (StateFlow<XUiState>)
                        ▼
                   XRoute → XScreen (Compose)
```

**When the full 5-layer scaffold isn't needed:** for a one-shot, non-cached read (like `stocks` briefly did in its early form), a lighter 2-file `core/<domain>` repository talking straight to `FirebaseFirestore` is acceptable and already precedented. Don't force the full stack where nothing needs caching.

## 2. How API calls are made — two transport strategies

The app does **not** use a single networking strategy. Which one a domain uses depends on whether the backend fronts that Firestore collection with an HTTP endpoint.

### A. Retrofit + Moshi, against a Cloud Functions Express API (the default)

Most domains (`indicators`, `news`, `marketRisk`, `posture`, `summary`, `weeklyPlaybook`, `stocks`) call a REST API exposed by the backend's `functions/src/api/marketPulse.ts` — an Express app deployed as a single Cloud Function, `api`. Each route is a thin wrapper that reads one Firestore document/collection and returns its JSON as-is.

On the Android side:
- `network/api/<Domain>Api.kt` — a plain Retrofit interface (`@GET("indicators/synthesis")`).
- All domain `Api` interfaces share **one** `Retrofit` instance, `@Named("MarketPulseRetrofit")`, built once in `network/retrofit/NetworkApiModule.kt` against `Constants.MARKET_PULSE_BASE_URL`, with a `MoshiConverterFactory` and an `OkHttpClient` that (optionally) carries `AppCheckInterceptor` + logging.
- `network/model/<domain>/Network<X>.kt` — `@JsonClass(generateAdapter = true)` DTOs, `val` properties, `@Json(name = "snake_case")` mapping. Pure Moshi models — no Firestore annotations.
- `Remote<Domain>DataSourceImpl` calls the `Api`, catches exceptions, maps `Network<X> → Domain<X>` via the domain's mapper file, and returns `Result<Domain<X>>`.

A few domains fan out concurrent calls and merge them — `RemoteIndicatorsDataSourceImpl` fires 5 `async { api.get...() }` calls (the "Five Pillars") and assembles one `MarketIndicators` object; `RemoteStockDataSourceImpl` fires one `api.getStockAnalysis(symbol)` per tracked ticker concurrently and treats a `404 HttpException` per-symbol as "not analyzed yet" rather than failing the whole batch.

### B. Direct Firestore client SDK reads (no backend endpoint)

`dashboard` (`market_overview` collection) has no REST endpoint — backend jobs write straight to Firestore with nothing in front. So `RemoteDashboardDataSourceImpl` injects the shared `FirebaseFirestore` singleton (provided once in `di/FirebaseModule.kt`) directly and either:

- keeps a live `addSnapshotListener(...)` wrapped in `callbackFlow` (dashboard does this — market-open/closed state and live asset prices stream in continuously), or
- does a one-shot `.get().await()` (`kotlinx.coroutines.tasks.await`) for on-demand reads.

DTOs used this way carry **both** Moshi (`@Json`) and Firestore (`@get:PropertyName`/`@set:PropertyName`) annotations with `var` properties and no-arg-constructor-safe defaults, because `DocumentSnapshot.toObject()` needs reflection-friendly classes. `stocks` used to work this way — see §6 — but was migrated to strategy A once the backend added `/stocks/:symbol` and `/stocks/tracked`.

## 3. Caching — Room

Every domain except the ones intentionally left one-shot-only caches its data in a single shared Room database, `AppDatabase` (currently schema **version 16**, `di/DatabaseModule.kt` builds it with `DatabaseMigrations.ALL_MIGRATIONS` applied — no destructive fallback).

- **Entities** (`storage/database/entity/`) mostly mirror the domain model 1:1. Two row-shapes exist:
  - *Singleton-per-day/id* (`IndicatorsEntity` keyed by `dateId`, `MarketPostureEntity` keyed by a fixed `id`, `MarketPulseEntity` — declared in `SummaryEntity.kt` — keyed by `dateId`) — one row holds the whole domain's current snapshot.
  - *Multi-row-per-symbol* (`AssetOverviewEntity` keyed by `symbol`, `StockEntity` keyed by `symbol`) — used when a domain is naturally a list of independent items.
- **Nested objects don't get their own tables.** A domain's rich nested structure (e.g. `StockEntity.technicalIndicators`, `.executiveThesis`, `.topNewsStream`) is stored as a single JSON text column, (de)serialized by a per-domain `*Converters.kt` class using a local `Moshi.Builder().add(KotlinJsonAdapterFactory())` instance. This keeps the domain model reusable as-is between the Entity and the app layer. **A caveat learned the hard way (`market_pulse`, 2026-08-18):** this JSON-column caching means a *nested field's Kotlin type* changing (e.g. a `String` becoming an enum) is a breaking schema change too, even when the SQL column itself (still just `TEXT`) doesn't need a migration — a row cached by the older type crashes Moshi's default enum adapter on read (`JsonDataException: Expected one of [...] but was <old raw value>`). Any such change needs its own migration bump (even a no-op `DELETE FROM` to force a re-fetch), the same as an actual column change would. **A narrower alternative (`indicators`, 2026-08-22 `schema_version 2` rewrite):** if the *entire* nested object is already an optional (nullable) field on the containing domain model, a version bump isn't strictly required — wrapping the converter's `fromJson` in a `try/catch (JsonDataException)` that logs and returns `null` lets a stale-shaped cached row degrade to the same "no data yet" state every consumer already null-checks for, forcing a clean refetch instead of a migration. Reach for the real migration bump when the field isn't nullable, or when "just refetch" isn't an acceptable degrade for that domain.
- **DAOs** expose a `Flow<Entity?>` / `Flow<List<Entity>>` read stream plus `REPLACE`-conflict inserts, and (per-domain) a `getLastSyncedTimestamp()` / `updateLastSyncedTimestamp()` pair used purely for sync bookkeeping (§4) — not for cache-expiry logic. **There is no TTL/staleness check anywhere;** a cached value is shown until something explicitly tells the app to refresh.
- **`LocalXDataSourceImpl`** is the only thing that touches the DAO — it maps `Entity ↔ Domain` via the mapper file and is the one place `Flow<Entity>.map { it.toDomain() }` happens.

## 4. Keeping the cache fresh — `SyncManager`

`core/sync/SyncManager` is a single `@Singleton` that holds one Firestore `addSnapshotListener` on `system/sync_status` — a document the backend updates via a shared `updateSyncRegistry(db, flagName)` helper every time it finishes a job (e.g. `market_news_updated`, `stock_analysis_eod`, `weekly_playbook_actuals_updated`).

Every screen's ViewModel calls `syncManager.startListening()` in `onStart()` and `stopListening()` in `onStop()` — the listener itself is idempotent (`if (listenerRegistration != null) return`), so multiple screens calling it concurrently is safe and expected. When a snapshot arrives, `SyncManager` walks a fixed list of domain blocks, each doing:

```kotlin
val newTime = snapshot.getLong("<domain>_updated") ?: 0L
val localTime = xRepository.getLastSyncedTimestamp() ?: 0L
if (newTime > localTime) {
    xRepository.refresh(force = true)
    xRepository.updateLastSyncedTimestamp(newTime)
}
```

This is what makes the app reactive without polling: the client isn't guessing when to refetch — the backend flags exactly what changed, and only that domain's `refresh...()` (Remote fetch → Local save) fires.

`stocks` is the one domain with **two** flags feeding the same repository (`stock_analysis_eod` / `stock_analysis_after_hours`, from the EOD and after-hours backend runs), combined with `maxOf(...)` — the same pattern `weeklyPlaybook` uses for its Sunday-generation vs mid-week-actuals updates.

Repositories also self-trigger a `refresh...(force = false)` from their owning ViewModel's `onStart()` as a belt-and-braces "don't wait for the next Firestore write" pre-warm — `SyncManager` handles *ongoing* freshness, the ViewModel's own `onStart()` handles *first paint*.

## 5. Dependency injection — Hilt

Four centralizing modules everything else plugs into:

- **`FirebaseModule`** — the one `FirebaseFirestore` singleton, used both by direct-Firestore repositories and (implicitly) by the Firebase SDK's App Check integration.
- **`NetworkApiModule`** — the one shared `Moshi`, the one `OkHttpClient` (`@Named("MarketPulseClient")`), the one `Retrofit` (`@Named("MarketPulseRetrofit")`), and one `provide<Domain>Api(retrofit): <Domain>Api = retrofit.create(...)` function per domain.
- **`DatabaseModule`** — the one `AppDatabase`, and one `provide<Domain>Dao(db): <Domain>Dao` per domain.
- **`FinnHubApiModule`** — a *separate* OkHttp/Retrofit stack (different base URL, different auth interceptor) plus the WebSocket client, kept intentionally isolated from the `MarketPulseRetrofit` stack.

Each domain's own `di/<Domain>Module.kt` wires its own bindings. For binding conventions and code examples, see `CLAUDE.md`.

## 6. Presentation layer

### ViewModel + UiState

Every `@HiltViewModel` follows the same shape: constructor-inject the domain `Repository` (+ `SyncManager`), hold private `MutableStateFlow`s for `isLoading`/`isRefreshing`/`errorMessage`, `combine()` those with the repository's `Flow<Domain>` into one public `val uiState: StateFlow<XUiState>` via `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = XUiState(isLoading = true))`.

`XUiState` is a flat `data class` (nullable/default fields) in every domain except `summary`, which uses a sealed `Loading/Success/Error` interface — the one deliberate outlier, used because that screen genuinely renders different layouts per state rather than just toggling flags.

### Route / Screen split

Every screen is two composables:
- **`XRoute`** — stateful. Calls `hiltViewModel()`, `collectAsStateWithLifecycle()`, wires a `DisposableEffect(LocalLifecycleOwner) { LifecycleEventObserver { ON_START → viewModel.onStart(); ON_STOP → viewModel.onStop() } }`, owns the `PullToRefreshBox`/`SnackbarHost`, and — for screens that are pushed rather than tab-hosted (`stock_detail`-style pushes, `News`, `Settings`, `Indicator Horizons`) — its own `Scaffold`/`TopAppBar` with a back button.
- **`XScreen`** — stateless. Takes plain data + lambdas, no ViewModel awareness.

### Navigation

`ui/navigation/PulseNavGraph.kt` is the single `NavHost`. Two kinds of destinations:
- **Bottom-nav tabs** (`Overview`, `Indicators`, `Summary`, `Insights`, `Analysis`) — navigated to with the tab-preserving pattern (`popUpTo(startDestination) { saveState = true }; launchSingleTop = true; restoreState = true`), so switching tabs doesn't lose each tab's scroll position/back stack.
- **Pushed destinations** (`webview/{encodedUrl}`, `market_news`, `settings`, `indicator_horizons`) — plain `navController.navigate(route)`, popped with `navController.popBackStack()`. `News` used to be a bottom-nav tab; it's now reachable only by tapping into it from the Dashboard's news preview, which is why it grew its own `TopAppBar`. `indicator_horizons` (2026-08-22) is the newest of these — it started as local Compose state inside the Indicators tab's own screen and was promoted to a real destination specifically so `MainActivity`'s `isPushedDestination` check could exclude it from the global top bar and floating nav, which a same-composable-tree toggle has no way to do.

One-shot cross-screen signals (e.g. "scroll the News list to this specific article after a Dashboard preview-card tap") are **not** passed as nav arguments — they're hoisted as plain `remember { mutableStateOf(...) }` state inside `PulseNavGraph()` itself and consumed-then-nulled by the destination, specifically so they don't disturb the bottom-nav tab's plain route-string identity (a query-param route would break the `currentDestination.route == item.route` selected-tab check).

**A third case: a contextual jump into another tab's content** (Summary's Drivers section → Indicators tab). This isn't a real tab switch (the user didn't tap the bottom bar) but should still land on the same live Indicators instance a real tab switch would, and system back should return to Summary specifically, not fall through to whatever plain-push default back behavior would pick. Two things had to both be true, learned by getting each wrong once first:
- The navigation call itself uses the *identical* tab-switch `popUpTo`/`launchSingleTop`/`restoreState` block a real bottom-nav tap uses (not a plain push) — mixing "plain push" and "restoreState tab switch" navigation to the same route left Navigation-Compose's saved-state bookkeeping confused, silently no-oping later taps on other tabs.
- The `BackHandler` that redirects back-to-Summary has to be composed *inside* the Indicators destination's own `composable(...)` content in `PulseNavGraph.kt`, not up in `MainActivity`. `NavHost` registers its own internal back handling as part of composing itself; a `BackHandler` composed (and so added to the back-press dispatcher) *before* `NavHost` always loses to it, since the dispatcher processes the most-recently-added enabled callback first. One composed as a child of `NavHost` — inside the active destination's content — is added after, and only then actually takes priority for that screen. The flag gating it (`reachedIndicatorsFromDrivers`) is still owned by `MainActivity` (set via a callback threaded through `PulseNavGraph`'s params) and cleared the moment the route becomes anything other than Indicators, so it never lingers into an unrelated later visit to that tab.

**A fourth case: reporting screen-loaded data up to `MainActivity`'s chrome.** The global top bar is normally a static per-route title (`MainActivity` stays deliberately dumb about screen internals), but Summary's report-type label ("Daily Update"/"Weekend Update") is only known once that screen's own data loads. Rather than have the top bar reach into `SummaryViewModel`'s state directly, the value is reported *up* the same way `reachedIndicatorsFromDrivers` is reported down: `MarketSummaryRoute` fires a `LaunchedEffect`-driven callback (`onReportTypeLoaded`) the moment its `Success` state carries a `ReportType`, threaded through `PulseNavGraph`'s params to a `mutableStateOf<ReportType?>` held in `MainActivity`, which `topBarTitle()` reads with a static-string fallback for the not-yet-loaded case. Same shape as the Drivers-flag callback above — this is now the precedent to reuse for any future "top bar needs a value only some destination's data actually has."

### Status colors

`PulseStatusColors`/`ColorExtension.kt`'s `SignalColor.toColor()/toBgColor()` are gone (deleted in the theme migration; don't resurrect the names). The current system: `ui/theme/Color.kt`'s `PulseTokens.Signal` is the locked signal-color layer (bullish/bearish/neutral/warning, identical across every preset in a given mode), read via `LocalPulseColors.current` — never `MaterialTheme.colorScheme` for anything the token contract defines separately. Any domain model that carries a `SignalColor` (or `RiskImpactLevel` — the `market_risk` domain's HIGH/MEDIUM/LOW severity vocabulary, reused for risk/horizon reads elsewhere; or the indicators domain's `AlignmentState`/`AgreementState`/`ShiftDirection`) gets its colors through `ui/theme/SignalColorExtensions.kt`'s `.textColor`/`.pillColor` extensions, never a hand-rolled `when` in a composable. The two aren't interchangeable: `.pillColor` is deliberately a soft/pastel tint meant to sit *behind* a bolder color (typically its own paired `.textColor`) — using it as a standalone foreground (a marker dot, a filled meter bar, a plain accent `Text` color, or a 1dp outline stroke) reads as washed-out, a mistake made and then fixed more than once (Summary's rework, then again in the indicators revamp's outlined pills). `ui/components/widgets/SignalPill.kt` has an `outlined: Boolean` variant (transparent fill + a `contentColor`-toned `border_medium` stroke, not a `pillColor`-toned one, for the same washed-out reason) for supporting-context states that shouldn't compete visually with a card's own primary filled pill — the indicators domain's alignment/agreement/shift-direction pills all use it.

## 7. A worked example: `stocks`

Useful as a complete tour of the pattern, and as the domain with the most interesting history in this codebase:

1. Backend (`functions/src/scheduled/stocks/stockAnalysisEngine.ts`) computes technicals + an AI "deep study" per Magnificent-7 symbol and writes `market_stocks/{TICKER}`, plus flags `stock_analysis_eod`/`stock_analysis_after_hours` on `system/sync_status`.
2. Originally the Android app read `market_stocks` straight off the Firestore SDK (mirroring `dashboard`). Once the backend exposed `GET /stocks/tracked` and `GET /stocks/:symbol` in `marketPulse.ts`, `RemoteStockDataSourceImpl` was migrated to Retrofit — the `NetworkStockDeepStudy` DTO was simplified from a dual Moshi+Firestore model down to a plain Moshi one in the process.
3. `StockEntity` (keyed by `symbol`) + `StocksConverters` cache the result in Room; `StockAnalysisRepository` exposes `getTrackedStocksStream(): Flow<List<StockAnalysis>>` and `refreshTrackedStocks(force): Result<Unit>`.
4. `StockAnalysisViewModel` holds the chip-selection state (`selectStock(symbol)`) itself — no default selection; nothing is shown until the user taps a chip.
5. The whole feature was originally a widget embedded in the Dashboard (chip row inline, tapping a chip pushed a detail screen), then consolidated into its own bottom-nav tab (`Analysis`), replacing `News` there — `News` moved the other direction, from a tab to a Dashboard-preview-triggered push screen.

## Cross-repo contracts

The Android side has invisible dependencies on backend field/flag names:

- **Sync flag names** (`stock_analysis_eod`, `market_news_updated`, etc.) must match `updateSyncRegistry` calls in the backend engines. Any rename is a two-repo change.
- **Direct Firestore reads** (`market_overview`, historically `market_stocks`) rely on `@get:PropertyName`/`@set:PropertyName` matching backend field names. Silent break if either side changes alone.
- **Retrofit response shapes** must match backend Express route bodies. Fails loud (deserialization error), but still a coordinated change.

The canonical inventory of flags, collections, and their producers is in the Notion `10 — Architecture` page.
