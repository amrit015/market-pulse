# CLAUDE.md — Market Pulse (Android)

Kotlin / Jetpack Compose (Material 3) client for Market Pulse. Package root: `com.marketlabs.pulse` (not `com.marketpulse.app`). Backend is Firebase Cloud **Functions v2** — not Cloud Run.

Read this file first. Then read what you need for the task:
- `ARCHITECTURE.md` — how the system actually works (data flow, transport strategies, Room caching, SyncManager, presentation layer, worked example). Load when you're touching something unfamiliar or cross-cutting.
- Notion brain (link: `https://app.notion.com/p/marketPulse-brain-3b07c8397e7b801abfc8f8ceb1d9fdae`) — product context, ADRs, cross-repo contracts (sync flag names, Firestore field shapes), design system. Ask before pulling large pages.

## Stack

Kotlin, Jetpack Compose (Material 3), Dagger Hilt, Room, Retrofit + Moshi, Firebase Firestore (client SDK for live-streaming domains, backing store for all), Compose Navigation, Vico (charts), Compose-Markdown.

## The one governing rule

**Every domain is vertically sliced across the same layers, and data flows one direction: Remote → Local (Room) → Repository → ViewModel → Screen.** A ViewModel never talks to a data source directly. A Screen never talks to a ViewModel's dependencies — only to the `StateFlow` it exposes.

See `ARCHITECTURE.md §1` for the full per-domain package layout and §2 for the two transport strategies (Retrofit vs direct Firestore SDK).

## Commands

```bash
# Build and install debug on connected device
./gradlew installDebug

# Just compile
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run lint
./gradlew lint
```

## Conventions

Follow these unless you have a specific reason not to — and if you do, flag it before writing code.

### Dependency Injection (Hilt)

**100% `@Provides`-in-`object` style — no `@Binds`/abstract modules anywhere.** Three-tier domains get exactly three providers (remote data source, local data source, repository). Always `Impl → Interface`, always `@Singleton`.

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

Never introduce `@Binds` — it'd be inconsistent with all 9 existing modules.

### ViewModels & UiState

Flat `data class` UiState by default:

```kotlin
data class DashboardUiState(
    val marketState: MarketState? = null,
    val assets: List<AssetOverview?> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)
```

`@HiltViewModel`, constructor-inject `Repository` + `SyncManager`, expose one `val uiState: StateFlow<...>` via `combine(...).stateIn(viewModelScope, WhileSubscribed(5000), initial)`. Wire `onStart()`/`onStop()` to `syncManager.startListening()/stopListening()`. Full pattern in `ARCHITECTURE.md §6`.

**Use sealed UiState only when the screen genuinely renders different layouts per state**, not just different flags. `summary` is the one existing outlier — don't create a second one without a real reason.

### Compose screens

Every screen is two composables:
- **`XRoute.kt`** — stateful. `hiltViewModel()`, `collectAsStateWithLifecycle()`, wires `onStart`/`onStop` via `LifecycleEventObserver`, owns pull-to-refresh + snackbar.
- **`XScreen.kt`** — stateless. Plain data + lambdas, no ViewModel awareness.

Bottom-nav tabs use the tab-preserving navigation pattern (`popUpTo(startDestination) { saveState = true }; launchSingleTop = true; restoreState = true`). See `ARCHITECTURE.md §6` for the navigation graph shape.

### Page tabs (`PulseTabRow`)

**`ui/components/PulseTabRow.kt` is the one tab-bar design for every screen that switches between a handful of sibling sections** — a horizontally-scrolling row of segmented-control-style chips: solid `accentPrimary` fill + `accentOn` text for the selected chip, outlined `accentSurfaceBorder` hairline + `onSurfaceMuted` text for the rest, `corner_radius_small` shape. Established on the Stock Analysis detail screen (originally a private `DetailPillTabRow`), now shared with Insights. Never hand-roll a new tab bar (`TabRow`, `ScrollableTabRow`, a bespoke `Row` of `Surface`s) — call `PulseTabRow`.

The full pattern (see `StockDetailViewModel`/`StockDetailScreen`/`StockDetailRoute` or `InsightsViewModel`/`InsightsScreen`/`InsightsRoute` for worked examples):
- A per-screen `enum class XTab(val labelRes: Int)`, one entry per tab.
- The ViewModel keeps the selected index as `MutableStateFlow<Int>`, folded into the screen's `UiState` as `selectedTabIndex: Int`, with an `onTabSelected(index: Int)` setter.
- The Route renders `PulseTabRow(tabs = XTab.entries.map { stringResource(it.labelRes) }, selectedTabIndex = uiState.selectedTabIndex, onTabSelected = viewModel::onTabSelected)` pinned above the scrollable/pull-to-refresh content area, so tabs stay reachable regardless of what that area is showing (loading/error/data).
- The Screen branches on `XTab.entries[selectedTabIndex]`, each tab as its own `LazyColumn` with its own `LazyListState` (`remember { List(XTab.entries.size) { LazyListState() } }`) so scroll position survives switching tabs and back.

`ChartRangePicker` shares this same visual language (fill/outline treatment, `corner_radius_small`, `labelMedium` bold) but is intentionally a separate component, not a `PulseTabRow` caller — it's an evenly-weighted range selector (`Modifier.weight(1f)` per button, no scrolling), a different layout shape for a different job (picking a chart's time range, not switching between sibling page sections).

### Glossary content

**Every glossary lives in `core/glossary/` as a bundled `assets/*.json` file + a matching provider — never a hardcoded Kotlin object, never a batch of `*_def` strings in `strings.xml`.** Two shapes:
- **Flat term → definition** (`GlossaryTerm(term, definition)`): `market_glossary.json`/`MarketGlossaryProvider`, `risk_glossary.json`/`RiskGlossaryProvider`, `stock_analysis_glossary.json`/`StockAnalysisGlossaryProvider`, `dashboard_glossary.json`/`DashboardGlossaryProvider`. Nested categories (e.g. market's `regimes`/`setups`/`directions`/`cycle_zones`/`actions`) are just multiple `term -> definition` objects under one JSON file.
- **Richer per-metric shape** (`what_it_is`/`how_to_read`/`bands`/`gotchas`): `metric_glossary.json`/`MetricGlossaryProvider`, for Indicators/Positioning/Posture's per-metric detail page, which needs bands and a "gotchas" caveat, not just a one-line definition.

Provider access pattern differs by consumer, and this is deliberate, not inconsistent: `MetricGlossaryProvider` is a Hilt `@Singleton @Inject constructor(@ApplicationContext context: Context)` class because its only consumers are `@HiltViewModel`s. The other 4 providers are plain lazily-cached singleton `object`s taking `Context` as a parameter (call `.get(LocalContext.current)` / `.definitionFor(LocalContext.current, key)` directly from the composable) because their call sites are deeply nested, stateless leaf composables reached from a dozen+ screens with no ViewModel in between — threading Hilt through every intermediate screen's ViewModel/UiState would buy nothing over a process-cached in-memory map. Match whichever pattern fits a new glossary's actual call sites; don't force Hilt onto a leaf-composable-only glossary just for consistency with `MetricGlossaryProvider`.

### Resources

- **All UI text** → `res/values/strings.xml`, referenced via `stringResource()`. Naming: `snake_case`, loosely `<screen_or_context>_<purpose>` (e.g. `news_screen_title`, `radar_vulnerability_score`).
- **All dimensions** → `res/values/dimens.xml`, referenced via `dimensionResource()`. Prefer existing semantic dimens (`padding_small`, `corner_radius_medium`) over inventing one-offs.
- **Status colors** → since spec-20260809-theme-migration, the old `PulseStatusColors` object is deleted. The signal layer (`PulseTokens.Signal` in `ui/theme/Color.kt`) is locked and identical across every preset in a given mode; extended-token reads go through `LocalPulseColors.current` (accent group, `surfaceTinted`, `onSurfaceMuted`), never `MaterialTheme.colorScheme` for anything the Token Contract defines separately. If a domain model carries `SignalColor`, use the `SignalColor.textColor`/`.pillColor` extensions (`ui/theme/SignalColorExtensions.kt`) — never hand-roll a `when` branching on the enum.
- **Card shadows go through `PulseCard`, not a one-off `Modifier.shadow`/`CardDefaults.cardElevation` call.** As of the 2026-08-29/31 card redesign, every `PulseCardStyle.DATA`/`SYNTHESIS`/`DATA_SPARKLINE` card casts a shadow (hand-applied via `Modifier.shadow` inside `PulseCard.kt`, ambient-weighted over spot so it reads as an even halo rather than Android's default bottom-heavy one) — this app is no longer flat/no-shadow by convention, that rule is retired. `FloatingBottomNav` (`ui/components/FloatingBottomNav.kt`, `nav_elevation` = 2dp) is a separate, still-standing one-off: genuine Material `tonalElevation`, not `PulseCard`'s custom-drawn shadow. See `ARCHITECTURE.md`'s "Theming — light/dark presets & the card system" for the full picture (card styles, per-preset background tint, the `animateContentSize()`-vs-shadow gotcha).

### Null handling

Default fields to `null`, not arbitrary defaults. The exception is Firestore DTOs consumed via `toObject()`, which need a no-arg constructor — those use `var` properties with cheap defaults only where a primitive genuinely can't be null (`var symbol: String = ""`, `var price: Double = 0.0`); everything else is `null`. Domain models are always immutable `val`, nullable default `null`.

### Cross-repo contracts

The Android side has invisible dependencies on backend field/flag names:
- **Sync flag names** in `SyncManager` must match `updateSyncRegistry` calls in the backend engines. Renaming one is a two-repo change.
- **Direct Firestore reads** (`market_overview`, historically `market_stocks`) rely on `@get:PropertyName`/`@set:PropertyName` matching backend field names. Silent break if either side changes alone.
- **Retrofit response shapes** must match backend Express route bodies. Fails loud (deserialization error), but still coordinated.
- **`last_updated` vs `timestamp` on backend responses.** Several backend domains — the stocks domain (`/stocks/previews`, `/stocks/{symbol}/detail`) and `market_indicators/ai_synthesis` (indicators domain) confirmed so far — include both a `last_updated` string (pre-formatted for human/Firestore-console readability, e.g. `"August 7, 2026 at 6:15:44 PM UTC-7"`) and a `timestamp` (epoch millis) in the same response. The app only ever consumes `timestamp` for "as of" display; `last_updated` is intentionally left unmodeled in the `Network*` DTOs (see the doc comments on `NetworkStockPreview`/`NetworkStockDetail`/`NetworkAiSynthesis`). Don't add `last_updated` back in — and check for this same pair before modeling a new backend response. **2026-08-22 incident:** the indicators schema_version 2 revamp modeled `last_updated` as a `String?` on `NetworkAiSynthesis` anyway (missed that this rule applied here too) and it broke JSON parsing against a live document — the field isn't reliably a plain JSON string. Removed; `timestamp` alone drives the executive hero's "Analyzed as of" display. Treat this rule as binding for every backend response, not just the two domains named above — check for the pair before modeling any new one.

Backend equivalents are documented in `Notion 10 — Architecture` and the backend `CLAUDE.md`.

## Strict rules

1. **Initialize as `null`, not arbitrary defaults.** Only add a default where it's genuinely necessary, and flag it before doing so.
2. **`@Preview` on every composable.** No exceptions. Use inline mock data. The gap in `InsightsScreen`, `NewsScreen`, and every `*Route.kt` is legacy debt — don't extend it (`*Route.kt` files are the one standing exception in practice, since they need a live Hilt `ViewModel` — previews belong on the stateless `*Screen.kt`/sub-composables they render). `SummaryScreen.kt` and, as of the 2026-08-22 indicators revamp, `IndicatorsScreen.kt`/`IndicatorDetailSheet.kt`/`IndicatorHorizonsScreen.kt` have full coverage — treat those as the model to copy, not the gap. Wrap preview content in `MarketPulseTheme(theme = MarketPulseTheme.<PRESET>) { ... }`, never plain `MaterialTheme { ... }` — almost every component reads `LocalPulseColors.current` (directly or via `.textColor`/`.pillColor`), which throws ("PulseColors not provided") outside `MarketPulseTheme`. A preview that compiles but is wrapped in the wrong theme still fails to render in the IDE with no obvious compile error to point at.
3. **No hardcoded strings or dimensions in Compose files.** Always `stringResource()` / `dimensionResource()`. `DashboardScreen.kt` has a hardcoded `"Sector Rotation"` and raw `dp` literals — don't propagate those into new code.
4. **Comment thoroughly. Preserve existing comments** unless verifiably obsolete.
5. **Explain before you code.** For every non-trivial change, start with a short paragraph on your approach and the choices you're making. Keep reasoning in prose, not comments.
6. **Localized changes only.** Do not refactor, reformat, or "clean up" outside the requested change. If you see something wrong, flag it — don't silently fix it.
7. **Ask before assuming.** Ambiguous requirement, missing color token, unclear data model, unfamiliar acronym — stop and ask.
8. **Comments are self-contained — never cite a spec, ADR, or doc file by name or requested changes.** No `spec-YYYYMMDD-*.md`, no "Token Contract," no "Design Direction," no "per the migration table." A comment has to make sense to someone with only this repo checked out, nothing else open. Document the thought process and the actual implementation directly: what the code does, why it does it that way, what it replaced and why that mattered. If a rule genuinely originates from an external doc, restate the rule itself in the comment — don't point at the doc.

## Known gaps (don't propagate)

Current state — none of these are the convention to follow. Fix opportunistically when the file is being touched for another reason; do not schedule cleanup work around them without an ADR.

- **`@Preview` coverage is incomplete.** Only `DashboardScreen.kt`, `MarketPostureView.kt`, `WeeklyPlaybookView.kt` have partial previews; `SummaryScreen.kt`, `IndicatorsScreen.kt`, `IndicatorDetailSheet.kt`, and `IndicatorHorizonsScreen.kt` have full coverage. The rest of the app is still the gap. Rule stands for new code regardless.
- **Broken (not just missing) previews in `MarketPostureView.kt`, `WeeklyPlaybookView.kt`, `DashboardScreen.kt`, `NewsScreen.kt`, `UnifiedScoreHeaderCard.kt`.** All wrap preview content in plain `MaterialTheme { ... }` instead of `MarketPulseTheme { ... }` — if the composable inside reads `LocalPulseColors.current` (most do, directly or via `SignalColorExtensions`), the preview crashes at composition time with "PulseColors not provided" instead of rendering. Found while fixing the identical bug in `SummaryScreen.kt`'s own previews. Fix opportunistically per file, same as everything else in this list.
- **Hardcoded strings/dims in `DashboardScreen.kt`.** `"Sector Rotation"` and raw `dp` literals bypass the resource system.
- **`DatabaseModule` missing `@Singleton` on two DAO providers.** `provideMarketSummaryDao` and `provideMarketPostureDao` — every other DAO provider has it. Cosmetic if you're not touching that module.
- **`NewsModule` / `MarketRiskModule` parameter naming.** Both name their repository impl parameter `marketSummaryRepositoryImpl` — copy-paste leftover from `SummaryModule`. Also cosmetic.
- **`DashboardApi.kt` is dead code.** Dashboard uses direct Firestore SDK, not REST. Do not "wire it up."

## Recent decisions (last 5)

- **2026-08-29/31 — Card redesign: shadow-based `PulseCard`, per-preset background tint, `DATA_SPARKLINE` carve-out.** `DATA` cards moved off their accent-tinted background onto `colorScheme.surfaceVariant` (white in light, a lighter grey step in dark) with a hand-applied `Modifier.shadow` (ambient-weighted over spot, so it reads as an even halo instead of Android's default bottom-heavy one) and no border in either mode — this retires the "flat/no-shadow by convention" rule under Resources above. `SYNTHESIS` (AI/curated content) keeps its own border but now computes its background differently per mode (dark: existing blend darkened 12% toward black; light: white blended 15% toward `accent.primary` — hue-tuned, not darkened) and picked up the same shadow. New `DATA_SPARKLINE` style pins Overview's sparkline-bearing asset cards to `DATA`'s pre-redesign look, since those cards were the one explicit "keep as-is" exception. Light mode's page `background` (`toColorScheme()` in `MarketPulseTheme.kt`) is no longer a flat shared token — it's now a pure-white base blended with 5% of the active preset's own `accent.primary`, so the page reads as tinted per preset instead of one grey for all 5 light presets; dark mode's background is untouched. Found and fixed along the way: `animateContentSize()` on a `PulseCard`'s own outer `modifier` clips straight through the shadow's rounded corners (moved to the inner content `Column` in every expandable `SYNTHESIS` card); pushed screens' own `TopAppBar`s were defaulting to `colorScheme.surface`, which no longer tracks the now-tinted `background` (explicit `colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)` added to `SettingsScreen.kt`/`IndicatorHorizonsRoute.kt`/`WebViewScreen.kt`, matching `NewsRoute.kt`/`AssetDetailRoute.kt`'s existing pattern); `MainActivity`'s hardcoded `initialValue = MarketPulseTheme.LILAC` was flashing dark chrome for a frame before the real persisted theme loaded from DataStore, fixed with a synchronous `runBlocking` read before `setContent`; `ThemePickerGrid` now splits presets by `.isDark` before pairing into rows (light always left column, dark always right), rather than `.chunked(2)`-ing the raw enum order, which broke once the light-preset count was odd. See `ARCHITECTURE.md`'s "Theming — light/dark presets & the card system" for the full mechanism.
- **2026-08-27 — All glossary content standardized on bundled JSON, not hardcoded Kotlin objects.** `MarketGlossary`/`RiskGlossary`/`StockAnalysisGlossary` (`utils/glossary/`) and Dashboard's 4 hardcoded `dashboard_glossary_*_def` strings all moved to `assets/*.json` + a matching provider in `core/glossary/` (`MarketGlossaryProvider`, `RiskGlossaryProvider`, `StockAnalysisGlossaryProvider`, `DashboardGlossaryProvider`), same pattern `MetricGlossaryProvider` established for Indicators/Positioning/Posture. These 4 new providers are deliberately plain lazily-cached singleton `object`s, not Hilt `@Singleton @Inject` classes like `MetricGlossaryProvider` — every call site (`MarketBottomSheet.kt`/`RiskBottomSheet.kt`/`StockAnalysisGlossaryBottomSheet.kt`/`AssetDetailScreen.kt`) is a deeply nested, stateless leaf composable reached from a dozen+ screens with no ViewModel in between, so they take `Context` (`LocalContext.current` at the call site) instead of being threaded through Hilt. Deleted `GaugeDictionary.kt` (`utils/glossary/`) as fully dead code — zero references anywhere. `IndicatorsDictionary.kt` (`utils/glossary/`) was initially kept on the (wrong) assumption that it was still used by 4 files; 3 of those were commented-out dead code, and the 4th (`FrameworkSheet.kt`) turned out to be unreachable too — its only trigger (`showFrameworkSheet = true`) was deleted from `IndicatorsScreen.kt` back in the "remove legacy daily pulse" commit (2026-06-25), leaving the `showFrameworkSheet` state and its `if` block orphaned ever since. Deleted `FrameworkSheet.kt`, the orphaned state/`if` block in `IndicatorsScreen.kt`, `IndicatorsDictionary.kt`, and `ContextHeaderCard` in `UniversalMetricCard.kt` (the only other live caller of `IndicatorsDictionary`'s `PillarGuide` type, itself dead — same "trigger removed, orphaned code left behind" pattern in `MarketActionTab.kt`/`MacroVitalsTab.kt`/`MarketPhaseTab.kt`, which still have several other commented-out calls into this same dead chain worth a closer look if those 3 files are ever revisited).
- **2026-08-27 — `PulseTabRow` established as the one page-tab design app-wide.** Pulled the segmented-chip tab bar out of `StockDetailRoute`'s private `DetailPillTabRow` into a shared `ui/components/PulseTabRow.kt`, then pointed Stock Analysis detail at it and gave Insights the same tab treatment. Insights moved from one long vertically-stacked scroll (Weekly Playbook / Tail Risks / Institutional Posture / Market Positioning sections separated by dividers) to 4 `PulseTabRow` tabs (Playbook/Risks/Posture/Positioning), each its own `LazyColumn` with its own `LazyListState`, mirroring `StockDetailScreen`'s `DetailTab` pattern exactly (`InsightsTab` enum, `InsightsViewModel._selectedTabIndex`, `onTabSelected`). `ChartRangePicker` deliberately stays a separate component despite sharing the same visual language — it's an evenly-weighted range selector, not a page-tab bar. See `Page tabs (PulseTabRow)` under Conventions above.
- **2026-08-22 — Indicators tab revamp: `schema_version 2` + UI rebuild + per-metric glossary.** Full-stack migration off the old `overarching_condition`/`pillar_glances`/per-horizon `briefing`/`key_driver`/`what_to_do` shape onto the backend's `executive`/`pillar_scorecard[]`/`horizons` structure (new `AlignmentState`, `AgreementState`, `ShiftDirection` enums; `RiskImpactLevel` reused for horizon risk, not a new enum). Two backend follow-up changes landed mid-migration and both required a same-day fix: `horizons.*.key_drivers[]` and `pillar_scorecard[].contributing_metric_ids` were removed from assembly entirely (deleted `DomainKeyDriver`/`NetworkKeyDriver` and the metric-name-resolution plumbing that existed only to support them); `alignment_with_macro`'s `TENSION` value was split into `MARKET_AHEAD_OF_FUNDAMENTALS`/`MARKET_BEHIND_FUNDAMENTALS` (both still colored as the same neutral/caution tone pending backend clarification on whether one should read bullish/bearish). New `core/glossary/` domain (bundled `assets/metric_glossary.json`, all 26 metrics, `MetricGlossaryProvider` singleton, no Hilt module needed — plain `@Inject constructor`) replaces the old fuzzy-name-matched `IndicatorsDictionary` for the metric detail sheet; band labels are written to match live `signal_text`, not a re-derived threshold. `Horizons` promoted from in-tab Compose state to a real pushed `NavGraph` destination (`IndicatorHorizonsRoute`/`Screen`, `PulseRoutes.INDICATOR_HORIZONS`) so it gets its own `Scaffold`/`TopAppBar` and is excluded from the global top bar + floating nav via `isPushedDestination`, same as Settings/News. `SignalPill` gained an `outlined` variant (transparent fill + `contentColor`-toned `border_medium` stroke — not `pillColor`, which is a pale background-tint token that read as washed-out at any stroke width) for supporting-context states (alignment/agreement/shift direction) that shouldn't compete visually with a card's primary filled pill. New shared `AnalyzedAtHeader` component (`ui/components/`) keeps the "Analyzed as of" line pixel-identical between Indicators and Summary. Summary's own in-content header (icon + report-type label + timestamp) is gone — the report-type label now drives the global top bar dynamically via a new up-reporting callback (`MarketSummaryRoute` → `PulseNavGraph` → `MainActivity`, mirroring `onDriversNavigatedToIndicators`'s existing shape), and only the timestamp stays, via `AnalyzedAtHeader`.
- **2026-08-19 — Summary tab rebuilt against `market_pulse` v2** (new `verdict.direction`/`.conviction`, `drivers[]`, `market_position`, `watch[]`, `risks[]`; full styled pass replacing the old `VerdictCard`/`call`-based design). Regime chip is tinted by `direction`, not a separate text pill; `setup` moved to Market Position; per-chip glossary chevrons replaced one card-wide tap. `drivers[].direction` means net effect on equities, not the underlying indicator's own reading (backend change, same date) — see `MarketDriver`'s doc comment in `SummaryModels.kt`. Contextual jumps into another tab's content (Drivers → Indicators) use the same tab-switch nav mechanism as the bottom bar, with a `BackHandler` placed *inside* the destination's own composable (not above `NavHost`) to return to the actual origin tab — see §6 "Navigation" in `ARCHITECTURE.md`.

Full entries in Notion `50 — Decisions`.
