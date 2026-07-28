# CLAUDE.md — MarketPulse (Android)

Architectural reference for this codebase, derived from an audit of existing feature domains. Package root: `com.marketlabs.pulse` (not `com.marketpulse.app`). See also `.claudecode` for the strict coding rules (nulls, `@Preview`, resource usage, no unnecessary refactors) — this file documents *how the code actually looks*, including where it currently violates those rules.

## Directory structure (per feature domain, e.g. `dashboard`, `posture`, `news`)

```
core/<domain>/<Domain>Repository.kt          interface
core/<domain>/<Domain>RepositoryImpl.kt      @Singleton @Inject impl
network/model/<domain>/Network<X>.kt         Moshi/Firestore DTOs
network/store/<domain>/Remote<Domain>DataSource.kt (+ Impl)
network/api/<Domain>Api.kt                   Retrofit interface (if backed by REST, not raw Firestore)
storage/model/<domain>/Domain<X>.kt          clean domain models (UI/repo-facing)
storage/model/<domain>/mappers/<Domain>Mappers.kt   Network→Domain, Network→Entity, Entity→Domain, Domain→Entity
storage/store/<domain>/Local<Domain>DataSource.kt (+ Impl)   wraps a Room DAO
storage/database/entity/<X>Entity.kt         Room entities (in the shared entity/ package, not per-domain)
storage/database/dao/<X>Dao.kt               Room DAOs (shared dao/ package)
di/<Domain>Module.kt                         Hilt bindings for the above
ui/screens/<domain>/<Domain>ViewModel.kt
ui/screens/<domain>/<Domain>UiState.kt
ui/screens/<domain>/views/<Domain>Route.kt   stateful, hoists ViewModel
ui/screens/<domain>/views/<Domain>Screen.kt  stateless, pure composable
```

Two data-fetch styles coexist — pick per how the backend serves the domain, don't force one:
- **Retrofit + Moshi** (majority): `network/api/<Domain>Api.kt` `@GET` interface → `Network<X>` DTO (`@JsonClass(generateAdapter = true)`, `@Json(name = "snake_case")`) → mapped to a `Domain<X>` model.
- **Direct Firestore reads** (`dashboard`/`market_overview`, `stocks`/`market_stocks`): inject `FirebaseFirestore` directly into a `Remote*DataSourceImpl`, either via `addSnapshotListener` wrapped in `callbackFlow` (live-updating domains) or `.get().await()` (`kotlinx.coroutines.tasks.await`, one-shot reads). The DTO class is dual-annotated with **both** `@Json`/`@JsonClass` (Moshi) *and* `@get:PropertyName`/`@set:PropertyName` (Firestore) so the same class satisfies `doc.toObject()` and stays consistent with the rest of the codebase's DTO shape — even domains with no Retrofit involvement keep both annotation sets.

## Null handling & DTO shape

Per `.claudecode` rule 1: default to `null`, not arbitrary defaults — but Firestore's `toObject()` requires a no-arg constructor, so DTOs consumed via direct Firestore reads use `var` properties with cheap defaults only where a primitive genuinely can't be null (`var symbol: String = ""`, `var price: Double = 0.0`); everything else defaults to `null`. Domain models (`storage/model/<domain>/Domain<X>.kt`) are plain immutable `data class` with `val`, nullable fields default `null`.

## Dependency Injection (Hilt)

**100% `@Provides`-in-`object` style — no `@Binds`/abstract modules anywhere.** Every module:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DashboardModule {
    @Provides
    @Singleton
    fun provideRemoteDashboardDataSource(
        remoteDataSourceImpl: RemoteDashboardDataSourceImpl
    ): RemoteDashboardDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository = dashboardRepositoryImpl
}
```

Three-tier domains get exactly 3 providers: `provideRemote<Domain>DataSource`, `provideLocal<Domain>DataSource`, `provide<Domain>Repository` — always `Impl → Interface`, always `@Singleton`. `FirebaseModule` provides the app-wide `FirebaseFirestore` singleton; `DatabaseModule` builds the single Room `AppDatabase` plus one DAO provider per feature. `PulseApplication` (`di/app/`) is `@HiltAndroidApp` and only bootstraps Firebase App Check — no bindings live there.

**Known inconsistencies** (don't silently "fix" without being asked — flag if touching):
- `DatabaseModule`'s `provideMarketSummaryDao`/`provideMarketPostureDao` omit `@Singleton` (the rest don't).
- `NewsModule.provideNewsRepository` / `MarketRiskModule.provideMarketRiskRepository` name their impl parameter `marketSummaryRepositoryImpl` — copy-paste leftover from `SummaryModule`, cosmetic only.

## ViewModels & UiState

Dominant convention (Dashboard, Insights, News, Indicators) — **flat `data class` UiState**, not sealed:

```kotlin
data class DashboardUiState(
    val marketState: MarketState? = null,
    val assets: List<AssetOverview?> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)
```

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val syncManager: SyncManager
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getMarketStateStream(),
        repository.getDashboardAssetsStream(),
        combine(_isLoading, _isRefreshing, _errorMessage) { l, r, e -> Triple(l, r, e) }
    ) { state, assets, (loading, refreshing, error) ->
        DashboardUiState(state, assets, loading && assets.isEmpty(), refreshing, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState(isLoading = true))

    fun onStart() { syncManager.startListening(); fetchDashboard(force = false) }
    fun onStop() { syncManager.stopListening() }
}
```

`Summary` is the **one outlier**: sealed interface `Loading` / `Success(data)` / `Error(message)`, consumed via `when(state)` in `SummaryRoute.kt`, plus a one-shot `Channel<String>` for snackbar events. Treat flat-`data class` as the pattern to follow for new domains unless there's a specific reason to branch UI structurally (sealed states earn their keep when the screen genuinely renders different layouts, not just different flags).

Shared shape across all 6 ViewModels: `@HiltViewModel`, constructor-injects `Repository` + `SyncManager`, exposes exactly one `val uiState: StateFlow<...>` (name it `<domain>UiState` if the screen isn't literally "the" state, e.g. `summaryUiState`), private `_isLoading`/`_errorMessage`/(`_isRefreshing`) `MutableStateFlow`s merged via `combine`, `onStart()`/`onStop()` driving `syncManager.startListening()/stopListening()`, and a `refresh<Domain>(force: Boolean)` that launches in `viewModelScope`.

## Compose screens

Consistent **Route/Screen split** for every feature: `<Domain>Route.kt` is stateful (`hiltViewModel()`, `collectAsStateWithLifecycle()`, wires `onStart`/`onStop` to `LifecycleEventObserver`, owns pull-to-refresh/snackbar), delegates to a stateless `<Domain>Screen.kt` (pure composable taking plain data + lambdas). Navigation is a single `PulseNavGraph.kt` with a `PulseRoutes` object of string constants and a `sealed class BottomNavItem`; bottom-nav screens are wired as `composable(route) { <Domain>Route(scaffoldPadding = innerPadding) }`.

**`@Preview` compliance is currently violated** against the `.claudecode` rule ("always include `@Preview`"): only `DashboardScreen.kt`, `MarketPostureView.kt`, and `WeeklyPlaybookView.kt` have any `@Preview` composables, and even there only a fraction of composables are covered. `InsightsScreen`, `IndicatorsScreen`, `NewsScreen`, `SummaryScreen`, and every `*Route.kt` have **zero**. Don't treat the absence of previews elsewhere as license to skip them on new composables — the rule stands; the gap is legacy debt, not the convention to copy.

**Resource usage is mostly but not fully compliant**: `DashboardScreen.kt` has a hardcoded `"Sector Rotation"` string (with its own `// Consider moving to strings.xml later!` comment) and raw `dp` literals bypassing `dimens.xml`. Same rule applies — don't propagate hardcoded strings/dims into new code just because one file has them.

## Resource management

- `res/values/strings.xml` (~235 lines): flat namespace, `snake_case`, loosely `<screen_or_context>_<purpose>` (`news_screen_title`, `news_empty_state`, `radar_vulnerability_score`, `gauge_recession`). Not strictly per-screen-prefixed — some keys are generic (`label_trigger`, `label_impact`) and reused.
- `res/values/dimens.xml` (~47 lines): semantic-role-named, not per-screen: `padding_tiny/small/medium/standard/large/xlarge/xxlarge`, `corner_radius_*`, `icon_size_*`, `border_*`, plus feature-specific one-offs (`gauge_*`, `vix_corner_radius`, `timeline_*`). Prefer an existing semantic-role dimen over inventing a new one-off.
- `PulseStatusColors` (`ui/theme/Color.kt`) is the canonical status color source — an `object` of `@Composable get()` `Color` properties branching on `isSystemInDarkTheme()`: `BullishText/BearishText/NeutralText/WarningText` (text/icon tier) and `BullishBg/BearishBg/NeutralBg/WarningBg` (card-background tier, alpha-blended in dark mode). Consumed directly in ~13 files, or indirectly via `utils/extensions/ColorExtension.kt`'s `SignalColor.toColor()` / `SignalColor.toBgColor()` extensions, which map the domain enum `SignalColor.GREEN/YELLOW/RED/UNKNOWN` onto it. **When a domain model carries a `SignalColor`, use the extension function — don't hand-roll a new `when` branching on the enum.**

## Rules to hold new code to

1. Match the 3-tier split (`core` interface+impl / `network` DTO+remote-source / `storage` domain-model+mapper+local-source) for any domain that needs offline caching or a live Firestore stream. For a one-shot, non-cached read (like `stocks`), a lighter 2-file `core/<domain>` repository talking straight to `FirebaseFirestore` is acceptable and already precedented — don't force the full 5-layer scaffold where nothing needs caching.
2. Bind everything with `@Provides` in an `object` module — never introduce `@Binds` into this codebase, it'd be inconsistent with all 9 existing modules.
3. New UiState → flat `data class`, not sealed, unless the screen has genuinely distinct render paths.
4. New screens → Route/Screen split, `@Preview` on every composable, `stringResource()`/`dimensionResource()` for all UI text/dims, `SignalColor.toColor()`/`toBgColor()` for status coloring.
