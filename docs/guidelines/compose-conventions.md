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
shared with Insights and Indicators. Never hand-roll a new tab bar (`TabRow`, `ScrollableTabRow`, a
bespoke `Row` of `Surface`s) — call `PulseTabRow`.

The full pattern (see `StockDetailViewModel`/`StockDetailScreen`/`StockDetailRoute`,
`InsightsViewModel`/`InsightsScreen`/`InsightsRoute`, or `IndicatorsViewModel`/`IndicatorsScreen`
for worked examples):

- A per-screen `enum class XTab(val labelRes: Int)`, one entry per tab. Tab labels can be a short
  nav label distinct from a longer inner-content heading the same string used to double as
  (Indicators' tabs read "Momentum"/"Macro"; each tab's own inner section heading still reads
  "Tactical Momentum"/"Macro Economy") — this is a deliberate split, not drift to reconcile.
- The ViewModel keeps the selected index as `MutableStateFlow<Int>`, folded into the screen's
  `UiState` as `selectedTabIndex: Int`, with an `onTabSelected(index: Int)` setter.
- **Tab content is swipeable**, not just tap-switched (2026-09-05 on Stock Detail/Indicators,
  matching Insights' own earlier `HorizontalPager` move): a `PagerState` from
  `rememberPagerState(initialPage = uiState.selectedTabIndex) { XTab.entries.size }`, kept in sync
  with `selectedTabIndex` via two one-directional `LaunchedEffect`s (tap → `animateScrollToPage`;
  swipe-settle, keyed on `pagerState.settledPage` **not** `currentPage`, → `onTabSelected`) — see
  `@docs/architecture/collapsing-header-tabs.md` for the exact effect shape and why `settledPage`
  specifically. `PulseTabRow` itself auto-scrolls to bring the selected chip into view whenever
  `selectedTabIndex` changes (a `BringIntoViewRequester` per chip) — needed once a swipe, not just
  a tap, can select a tab that's currently off-screen in the tab row itself.
- `PulseTabRow` renders
  `PulseTabRow(tabs = XTab.entries.map { stringResource(it.labelRes) }, selectedTabIndex = uiState.selectedTabIndex, onTabSelected = viewModel::onTabSelected)`
  pinned above the scrollable/pull-to-refresh content area, so tabs stay reachable regardless of
  what that area is showing (loading/error/data) — pinned outright on a screen with nothing above
  it that needs to scroll away (Insights); collapse-then-stick below some scrollable chrome on a
  screen that has some (Stock Detail, Indicators) — see
  `@docs/architecture/collapsing-header-tabs.md` for that heavier mechanism, only needed for the
  latter case.
- Each tab renders as its own `HorizontalPager` page, each with its own `LazyColumn`/
  `LazyListState` (`remember { List(XTab.entries.size) { LazyListState() } }`, hoisted above the
  pager so it survives pages scrolling in and out) so scroll position survives swiping/tapping away
  and back.

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

## "See more" navigation links

**`ViewMoreRow` (`ui/screens/stocks/detail/DetailSectionLabels.kt`) is the one shared structure for
any CTA that navigates to more content** — a bold accent-colored `text` plus a trailing forward
chevron (`ic_chevron_forward`): `ViewMoreRow(text = stringResource(...), onClick = ...)`. Covers a
capped list's "View More" link to its own fuller-list screen (Resolved Calls, Technical Timeline)
and a card's "Open full ..." entry point (Deep Dive) alike — don't hand-roll a new
`Row { Text(labelMedium.Bold, accentPrimary); Icon(ic_chevron_forward) }` for a new one. This is
**not** for in-place expand/collapse toggles (an up/down-arrow chevron, a different interaction
model — navigate away vs. reveal more of the same card) — those keep their own hand-rolled header
row (see card-heading-conventions.md's Seventh step for the exact split).

## Inline separator glyphs

Two different separator dots are in active use, and they're not interchangeable:

- **Middle dot (`" · "`, U+00B7)** — this app's long-standing plain-text join for two short fields
  on one line (`"{name} · {symbol}"`-style captions across many screens). No shared helper; just an
  inline `" · "` in a string template, unstyled.
- **Bullet (`R.string.bullet_separator`, "•", U+2022)** — a smaller, newer set of joins (an event's
  date + time, a relative-timestamp prefix, a bulleted list marker) that specifically want the dot
  rendered a size up from the surrounding text. **Never hardcode a literal `"•"` in a Kotlin
  string** — resolve `R.string.bullet_separator` and join via `buildBulletJoinedText`
  (`ui/components/widgets/BulletText.kt`), which builds the `AnnotatedString` with the bullet in a
  bumped `SpanStyle` font size. It's plain (non-`@Composable`) — callers resolve the string resource
  and the surrounding `TextStyle.fontSize` themselves and pass both in.

## Resources

- **All UI text** → `res/values/strings.xml`, referenced via `stringResource()`. Naming:
  `snake_case`, loosely `<screen_or_context>_<purpose>`.
- **All dimensions** → `res/values/dimens.xml`, referenced via `dimensionResource()`. Prefer
  existing semantic dimens (`padding_small`, `corner_radius_medium`) over inventing one-offs.
- **Status colors and card shadows** → see `@docs/theming-system/theming-spec.md`. Signal colors
  go through `LocalPulseColors.current`, never `MaterialTheme.colorScheme`, for anything the
  token system defines separately. Cards go through `ui/components/PulseCard.kt`, never a
  hand-rolled `Card(colors = ..., border = ...)`.
