# Compose conventions

Component- and resource-level conventions for writing new Compose UI in this repo. ViewModel/
UiState shape and the Route/Screen split themselves are documented as existing architecture in
`@docs/architecture/android.md` — this file is the prescriptive add-on: which shared components
to reach for, and how resources are organized.

**Status:** restructured 2026-08-31 from root `CLAUDE.md`'s Conventions section. Re-verified
against source during the move.

## Page tabs (`PulseTabRow`)

**`ui/components/PulseTabRow.kt` is the one tab-bar design for every screen that switches between
a handful of sibling sections** — a horizontally-scrolling row of segmented-control-style chips:
solid `accentPrimary` fill + `accentOn` text for the selected chip, outlined
`accentSurfaceBorder` hairline + `onSurfaceMuted` text for the rest, `corner_radius_small` shape.
Established on the Stock Analysis detail screen (originally a private `DetailPillTabRow`), now
shared with Insights. Never hand-roll a new tab bar (`TabRow`, `ScrollableTabRow`, a bespoke `Row`
of `Surface`s) — call `PulseTabRow`.

The full pattern (see `StockDetailViewModel`/`StockDetailScreen`/`StockDetailRoute` or
`InsightsViewModel`/`InsightsScreen`/`InsightsRoute` for worked examples):

- A per-screen `enum class XTab(val labelRes: Int)`, one entry per tab.
- The ViewModel keeps the selected index as `MutableStateFlow<Int>`, folded into the screen's
  `UiState` as `selectedTabIndex: Int`, with an `onTabSelected(index: Int)` setter.
- The Route renders
  `PulseTabRow(tabs = XTab.entries.map { stringResource(it.labelRes) }, selectedTabIndex = uiState.selectedTabIndex, onTabSelected = viewModel::onTabSelected)`
  pinned above the scrollable/pull-to-refresh content area, so tabs stay reachable regardless of
  what that area is showing (loading/error/data).
- The Screen branches on `XTab.entries[selectedTabIndex]`, each tab as its own `LazyColumn` with
  its own `LazyListState` (`remember { List(XTab.entries.size) { LazyListState() } }`) so scroll
  position survives switching tabs and back.

`ChartRangePicker` shares this same visual language (fill/outline treatment,
`corner_radius_small`, `labelMedium` bold) but is intentionally a **separate** component, not a
`PulseTabRow` caller — it's an evenly-weighted range selector (`Modifier.weight(1f)` per button,
no scrolling), a different layout shape for a different job (picking a chart's time range, not
switching between sibling page sections).

## Glossary content

**Every glossary lives in `core/glossary/` as a bundled `assets/*.json` file + a matching
provider — never a hardcoded Kotlin object, never a batch of `*_def` strings in `strings.xml`.**
Two shapes:

- **Flat term → definition** (`GlossaryTerm(term, definition)`): `market_glossary.json`/
  `MarketGlossaryProvider`, `risk_glossary.json`/`RiskGlossaryProvider`,
  `stock_analysis_glossary.json`/`StockAnalysisGlossaryProvider`, `dashboard_glossary.json`/
  `DashboardGlossaryProvider`. Nested categories (e.g. market's `regimes`/`setups`/`directions`/
  `cycle_zones`/`actions`) are just multiple `term -> definition` objects under one JSON file.
- **Richer per-metric shape** (`what_it_is`/`how_to_read`/`bands`/`gotchas`):
  `metric_glossary.json`/`MetricGlossaryProvider`, for Indicators/Positioning/Posture's
  per-metric detail page, which needs bands and a "gotchas" caveat, not just a one-line
  definition.

Provider access pattern differs by consumer, and this is deliberate, not inconsistent:
`MetricGlossaryProvider` is a Hilt `@Singleton @Inject constructor(@ApplicationContext context: Context)`
class because its only consumers are `@HiltViewModel`s. The other 4 providers are plain
lazily-cached singleton `object`s taking `Context` as a parameter (call
`.get(LocalContext.current)` / `.definitionFor(LocalContext.current, key)` directly from the
composable) because their call sites are deeply nested, stateless leaf composables reached from a
dozen+ screens with no ViewModel in between — threading Hilt through every intermediate screen's
ViewModel/UiState would buy nothing over a process-cached in-memory map. Match whichever pattern
fits a new glossary's actual call sites; don't force Hilt onto a leaf-composable-only glossary
just for consistency with `MetricGlossaryProvider`.

## Resources

- **All UI text** → `res/values/strings.xml`, referenced via `stringResource()`. Naming:
  `snake_case`, loosely `<screen_or_context>_<purpose>`.
- **All dimensions** → `res/values/dimens.xml`, referenced via `dimensionResource()`. Prefer
  existing semantic dimens (`padding_small`, `corner_radius_medium`) over inventing one-offs.
- **Status colors and card shadows** → see `@docs/theming-system/theming-spec.md`. Signal colors
  go through `LocalPulseColors.current`, never `MaterialTheme.colorScheme`, for anything the
  token system defines separately. Cards go through `ui/components/PulseCard.kt`, never a
  hand-rolled `Card(colors = ..., border = ...)`.
