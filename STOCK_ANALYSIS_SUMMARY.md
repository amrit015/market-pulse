# Stock Analysis Summary — `feat-stock-analysis`

A running log of the work done on this branch, in the order it happened. Written to hand off context (e.g. to a fresh Claude Chat session) without needing the full commit history or conversation transcript. Rationale/alternatives-considered detail for the decisions here lives in `ADR-2026-08-16-stock-analysis-post-spec-refinements.md` — this doc is the flat "what changed" inventory.

## 1. Base build — Preview list + Detail screen, per `spec-20260811-stock-analysis-ui`

Analysis bottom-nav tab (`StockAnalysisScreen`/`Route`/`ViewModel`) showing a scrollable list of `StockPreviewCard`s, and a full-screen Detail view (`StockDetailScreen`/`Route`/`ViewModel`) pushed on tap, at `stockAnalysis/{symbol}`. All 17 spec'd sections built as their own composables under `ui/screens/stocks/detail/sections/`, each skippable when its source data is null/empty. Loading/refreshing/error states on both screens, pull-to-refresh, chip/news expand-state in the ViewModel.

## 2. Jargon glossary bottom sheet (new pattern, not in spec)

Every `PulseCard(DATA)` metric card (Key Levels, Fundamentals, Macro, Headline Metrics, later Momentum & Trend, Returns) got one info icon next to its title (`DataCardTitleWithInfo`) opening `StockAnalysisGlossaryBottomSheet` — plain-English definitions for every metric that specific card shows, keyed by a stable internal term id (`utils/glossary/StockAnalysisGlossary.kt`), not the display label. Started as a per-metric tooltip, replaced with the per-card sheet once cards started grouping 4+ stats; a per-metric tooltip survives only where no wrapping card exists (`HeadlineMetricsStrip`'s siblings).

> **2026-08-27 update:** `utils/glossary/StockAnalysisGlossary.kt` is gone — content moved to `assets/stock_analysis_glossary.json` + `core/glossary/StockAnalysisGlossaryProvider.kt` as part of standardizing every glossary in the app onto bundled JSON. `StockAnalysisGlossaryBottomSheet` itself, its term-id keying scheme, and every call site are unchanged — this was a storage-backend swap only, not a content or UI change. See `CLAUDE.md`'s "Glossary content" convention.

## 3. Section relabeling, grounded in the actual backend field/prompt schema

- `PlainRead` → renamed `TechnicalRead` (the file was mapped to the wrong backend field; corrected).
- "Macro Transmission" → "Macro Impact"
- "Consider" → "Things to Check"
- "Event Log" → "Technical Timeline"

## 4. Polish pass across existing sections

- Dividers between Fundamentals' 4 clusters (now 5, see §11).
- Macro's stat row switched from a fixed `Row(SpaceBetween)` to `FlowRow` — "Rate Sens" (a full-word value like "MODERATE NEGATIVE") was crowding its neighbors.
- DirectNews impact badges colored by severity (HIGH/MEDIUM/LOW) instead of a flat outline.
- Bottom nav hidden on every pushed (non-tab) screen, not just Settings.
- Shared date formatter (`toLongDateString()`/`toAnalyzedAsOfString()`) applied across EventLog, ForwardCalls, DirectNews, Fundamentals, and both headers' "Analyzed as of" line.

## 5. EventLog: collapse to latest 7 + scroll-back fix

`EventLog` now shows the latest 7 entries by default with a "Show all"/"Show latest" toggle. Collapsing back to 7 needed a `BringIntoViewRequester` anchored to just the section's title row (not the whole section) to actually scroll the viewport back up — anchoring to the full section under-scrolled since `bringIntoView()` only moves the minimum needed to satisfy whatever bounds it's given, and a sliver of content was already in view.

## 6. Detail screen restructured: single scroll → 5 pinned-tab sections

Spec's 17-section linear list replaced with: `DetailHeader` + a tab row pinned, 5 independently-scrolling tabs (Technicals, Fundamentals, Thesis, Timeline, News) each with its own `LazyListState`. `TechnicalRead` + the highest-urgency `watch_list` item render as a shared leading block at the top of every tab (not pinned — tried pinned first, rejected for eating into the tabs' screen share).

Tab visual style iterated twice: Material3 `PrimaryTabRow` (underline indicator) → custom pill-chip segmented-control row (matching a Design reference) → tab corner radius softened from full pill (`corner_radius_pill`) to `corner_radius_small`, so the tab row stops looking like one more pill next to `DetailHeader`'s `technicalSetup` badge sitting directly above it.

> **2026-08-27 update:** This screen's own private `DetailPillTabRow` was pulled out into a shared `ui/components/PulseTabRow.kt` once Insights needed the identical tab pattern for its own 4-tab restructure (Playbook/Risks/Posture/Positioning). This screen now calls the shared component instead of a local copy — no visual or behavioral change here, it's the same look. See `STYLE_SPEC.md §15` and `CLAUDE.md`'s "Page tabs (`PulseTabRow`)" convention.

Final tab contents:
- **Technicals** — SetupReasoning, ChartPlaceholder, HeadlineMetricsStrip, MomentumAndTrend, Returns, KeyLevels, WatchList
- **Fundamentals** — Fundamentals, Macro
- **Thesis** — DeepStudy, Scenarios, Consider, SignalConditions, NotCoveredFooter
- **Timeline** — ForwardCalls (open + resolved), EventLog
- **News** — DirectNews

## 7. Card style system: `PulseCard(SYNTHESIS)` narrowed to 3 hero cards, app-wide

Contrast pass across all 10 presets found `SYNTHESIS`/`DATA` nearly indistinguishable on several, and with most cards using the "AI" tint it stopped meaning anything. `SYNTHESIS` now renders on exactly 3 cards app-wide — Summary's `VerdictCard`, Indicators' AI Executive Briefing, Dashboard's Technical Briefing (one hero card per screen). Everything else moved to `DATA`, including this feature's `StockPreviewCard`, `TechnicalRead`, `DeepStudy`, `Scenarios`, `DirectNews` — plus, outside this feature entirely, News' article cards, Summary's Lead Story/Macro/Domino/Outlook/Action Footer, and Insights' Weekly Playbook/Institutional Posture/Market Risks cards.

## 8. Color token fixes (app-wide, surfaced by this feature)

- `Accent.*.tinted` desaturated ~50% (HSL, saturation only) across all 10 presets — read too saturated once real card content sat on it. (Reference values supplied for 2 presets weren't reproduced exactly by HSL/HSV/RGB-average/luminance-weighted methods tried; standard HLS was closest and used, discrepancy flagged rather than hidden.)
- `onSurfaceMuted` (the one grey behind dates, "52W LOW"-style labels, every muted subtitle app-wide — 130+ call sites via `onSurfaceMuted`/`colorScheme.onSurfaceVariant`) lightness-shifted ~8% darker in light mode, ~8% lighter in dark mode for contrast.
- `PulseCard(DATA)`'s own background (`surfaceTinted`) and `PulseCard(SYNTHESIS)`'s (`accentSurfaceStrong`) reformulated so both derive from the same `tinted` base at different blend strengths, instead of two unrelated computations.

## 9. Typography fix: Detail screen's section headers were in the wrong type scale

`SectionDividerLabel`, `DataCardTitleLabel`, `SynthesisCardHeader` (shared by every Detail section) were using `labelLarge`/Inter — the small "terminal-style" font this app reserves for meta text like timestamps — instead of `titleLarge`/`titleMedium`/Montserrat, the scale every other screen's section headers use. Fixed to match `AssetCard` (`DataCardTitleLabel`) and Dashboard's Technical Briefing (`SynthesisCardHeader`).

## 10. Key Levels range bar (`PriceRangeBar`) rebuilt twice

First pass: added a missing resistance tick (previously undrawn — resistance was only used as the axis's own upper bound), inset the track from the rounded end caps, gave the price marker its own label instead of a bare dot.

Second pass, after the colors were found illegible: track/value-area fill switched from `accentSurfaceBorder`-based (measured contrast ~1.0–1.2 against the card, imperceptible, on every preset) to an `accentPrimary`-derived wash at 20%/55% opacity (verified numerically). Marker positions switched from literal dollar-proportional to ranked/evenly-spaced — proportional scaling reliably crowded support+price together while stranding resistance at the bar's own edge.

## 11. Chart placeholder (temporary, contradicts the spec's "leave it empty" instruction)

`ChartPlaceholder()` renders a visible tinted "Charts here" box in the Technicals tab's reserved chart region — added on purpose to visually verify the reserved space's layout before a real chart exists. **Must be swapped back to a bare `Spacer` before release.**

## 12. Field-coverage audit → new sections

A field-by-field audit of the actual backend JSON against every rendered section found ~40 unused `fundamentals` fields and ~17 unused `technical_indicators` fields (fetched, mapped, never rendered). Added:
- **Fundamentals**: 5th cluster "Balance Sheet & Risk" (market cap, beta, debt/equity, current ratio, price/book, price/sales, EV/revenue), a 5Y PE range line, Dividend Yield.
- **Technicals tab**: `MomentumAndTrend.kt` (MACD, dist SMA20/50, 52W high/low, up/down volume, vol percentile) and `Returns.kt` (1D/1W/1M/3M/6M/1Y), both after `HeadlineMetricsStrip`.
- **Timeline tab**: Resolved Calls sub-section (`calls.resolved[]`, previously entirely unused), collapsed-by-default with its own expand toggle; header stat extended with hit-rate % and failed count.

`setup_signals[]` confirmed genuinely redundant with `setup_confirming`/`setup_conflicting` (left unused, on purpose). `pe_percentile_5y` confirmed as a pure pass-through with no client logic reading it.

## 13. Direct News → its own tab, opens the article

Moved out of the Timeline tab into a new 5th "News" tab, sorted by `source_date` descending. Card is now clickable end-to-end, opening the article `url` in the same in-app WebView the Market News screen's cards already use (new nav route wiring through `StockDetailRoute` → `PulseNavGraph`). Kept as a separate affordance from the existing "Read the analysis" in-place expand toggle (forward_implication/transmission_mechanism), not a replacement for it. Added a divider between headline and body, and a chevron affordance.

## 14. List card: setup-change replaces chip-delta counter; pill styling; accent name

- `technical_setup` + "N new · M dropped" chip-delta counter → counter removed entirely; shows `"OLD → NEW"` when `previous_setup != technical_setup` and `setup_changed` is true, otherwise just the current setup. Now rendered as the same `OutlinedBadge` pill `DetailHeader` uses for the identical field (was plain muted text first, upgraded after the cross-screen inconsistency was noticed).
- `chipsAdded`/`chipsRemoved` still drive which condition chips render as "new" in the chip row — only the standalone counter text is gone. Orphaned strings/function deleted.
- Company `name` renders in `accentPrimary` on both the list card and `DetailHeader` (was muted grey on both).

## 15. Resolved Calls made collapsed-by-default

Initially shipped always-visible with a "show latest 7 / show all" toggle (matching EventLog's pattern). Changed to fully collapsed by default — just a tappable header, nothing rendered until expanded — since resolved calls are historical record, not something needing attention. `BringIntoViewRequester` scroll-back fix (same technique as EventLog) kept for collapsing a long expanded list back down. `ForwardCallCard`/`ResolvedCallCard` both fixed to `fillMaxWidth()` — were sizing to their own content instead of the full card width.

## 16. Backend `revamp-stock-analysis` change audit

Backend added two new VALUATION condition-chip labels ("Overvalued"/"Undervalued") and a new "Dividend below treasuries" chip, changed the trigger conditions for two existing labels, and added `dividend_yield_pct` to `fundamentals`. Audited every call site: chip rendering is fully generic (color from `direction`, text from raw `label`) — **zero code changes needed** for the new/changed labels. `dividend_yield_pct` was genuinely new — wired through `NetworkFundamentals` → `DomainFundamentals` → mapper → Fundamentals' Valuation cluster (§12).

## Known gaps / before this ships

- `ChartPlaceholder()` (§11) needs to come out.
- Nothing on this branch is committed yet — `git status` is still a full working-tree diff.
- Navy light hasn't had its own dedicated Design verification pass (spec called for one after Lilac dark).
- `ADR-2026-08-16-stock-analysis-post-spec-refinements.md` has the full rationale/alternatives-considered detail for anything summarized tersely above.
