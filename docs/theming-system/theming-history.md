# Theming history

Why the theming/card system looks the way it does — read this when you need the backstory behind
a decision in `@docs/theming-system/theming-spec.md`, not to find out what to call today. Chapters
below are in chronological order; most later chapters revise a decision an earlier one made.

**Status:** restructured 2026-08-31 from the prior root `DESIGN_MIGRATION_SUMMARY.md`,
`STOCK_ANALYSIS_SUMMARY.md`, `STYLE_SPEC.md`'s changelog notes, and `CLAUDE.md`'s theming-related
"Recent decisions" entries. Framing updated to past-tense/settled history — the originals were
written as in-progress branch logs ("not yet committed," "remains a placeholder") that are no
longer accurate; all of this has since merged.

## 2026-08-09/10 — Color system rebuilt from scratch: 10 theme presets

The old single-theme palette (`PulseBlue`/`PulseBlack`/`PulseGold`/`PulseOrange`/`AlertRed`,
`PulseStatusColors`, the legacy `ColorGreen`/`ColorRed`/`ColorNeutral` trio) was deleted entirely,
not deprecated. Replaced by `PulseTokens` (`ui/theme/Color.kt`) with three layers — Signal
(locked), Surface (locked per mode), Accent (the one thing that varies, 10 presets × 5 values). A
new Settings screen hosts the preset picker; selection persists via DataStore.

Two extended tokens were computed at runtime via `lerp()` from the start, not hand-picked hex, so
future "more/less pronounced" tweaks would be one-line factor changes instead of re-deriving 10
hex values by hand — `surfaceTinted` (price-card background) and `accentSurfaceStrong`
(AI/curated-card background). `PulseCard(style: PulseCardStyle)` consolidated every hand-rolled
`Card(colors=…, border=…)` call site in the app down to two styles, `DATA`/`SYNTHESIS` — a third,
`NEUTRAL` (plain elevated background, used briefly for VIX/Fear & Greed/Put-Call), existed and was
retired within the same pass once that distinction stopped being wanted.

`background` (light mode) was hand-tuned once during this pass — nudged lighter after the global
top bar started sharing this exact color, so it read as a clean "greyish-white" rather than
visibly cream.

Also landed in this pass: `SignalPill` consolidated every small colored badge into one component
(pill text blended 20%/40% toward black/white by mode); a text-color audit found several card
titles/body text reading `colorScheme.secondary`/`onSurfaceVariant` where it silently rendered
muted instead of full-strength (fixed, but flagged as an easy mistake to reintroduce — see
`@docs/architecture/known-gaps.md`); typography standardized to two title tiers by card style;
top-bar/status-bar edge-to-edge behavior fixed (a legacy `android:statusBarColor` override was
fighting `enableEdgeToEdge()`).

## 2026-08-16 — Stock Analysis ships; `SYNTHESIS` narrowed from ~24 cards to 3

Building the Stock Analysis tab end-to-end put real, dense card content against the token system
for the first time and surfaced a problem: `SYNTHESIS`/`DATA` read as nearly indistinguishable on
several presets once most cards on a screen wore the "AI" tint — it had stopped signaling
anything, it just meant "this app has two shades of card." `SYNTHESIS` was narrowed to exactly 3
cards app-wide — one hero card per screen (Summary's `VerdictCard`, Indicators' AI Executive
Briefing, Dashboard's Technical Briefing) — everything else, including Stock Analysis' own list
card and most of News/Insights, moved to `DATA`.

`accentSurfaceStrong`'s formula changed to match: instead of blending `accent.surface` toward
`accent.surfaceBorder` independently of `surfaceTinted`, it now started from the same
`accent.tinted` base `surfaceTinted` uses, blended toward `surfaceBorder` at a higher factor —
so the two card styles read as the same underlying color family at two intensities, not two
unrelated computations that happened to land in the same hue. `Accent.*.tinted` was desaturated
~50% across all 10 presets (it read too saturated once real content sat on top of it), and
`onSurfaceMuted` was lightness-shifted for contrast, based on measured contrast ratios per preset
rather than eyeballing.

Also in this pass: `PriceRangeBar`'s fill switched from an imperceptible-contrast
`accentSurfaceBorder` wash to a verified-contrast `accentPrimary` wash; a field-coverage audit
added several previously-fetched-but-unrendered sections; the Detail screen's tab bar (a private
`DetailPillTabRow` at the time) established the segmented-chip visual language `PulseTabRow` would
later be extracted from.

## 2026-08-19 — Summary rebuilt against `market_pulse` v2; `SYNTHESIS` becomes "usually 1, sometimes 2"

Summary's old single `VerdictCard` was replaced by two separate `SYNTHESIS` cards —
`SignalSection` (regime chip, one-sentence flash, conviction) at the top of the report, and
`TheReadSection` (full narrative, posture) at the bottom. "One hero card per screen" stopped being
a strict rule; Summary became the deliberate exception, carrying a hero card at both ends of the
same report rather than one in the middle. `OutlookCard`/`ActionFooter` (both `DATA`) were removed
entirely — their backing fields were dropped or renamed out of the API response, not just
restyled.

## 2026-08-22 — Indicators `schema_version 2`: `SignalPill` gains an `outlined` variant

The Indicators tab's full-stack rebuild (see `@docs/architecture/data-flow.md` for the backend-
shape side of this) needed supporting-context pills — alignment/agreement/shift-direction reads —
that shouldn't compete visually with a card's own primary filled pill. `pillColor` used as a
standalone stroke read washed-out, so `SignalPill` gained an `outlined: Boolean` variant
(transparent fill + a `contentColor`-toned `border_medium` stroke instead).

## 2026-08-27 — Glossary standardized on bundled JSON; `PulseTabRow` extracted

Two separate consolidations landed the same day, both covered in full in
`@docs/guidelines/compose-conventions.md` rather than here (neither is a *color* system change):
every glossary moved from hardcoded Kotlin objects / scattered `strings.xml` entries onto bundled
`assets/*.json` + a provider; the segmented-chip tab bar `StockDetailRoute` had been using
privately (`DetailPillTabRow`) was pulled into a shared `ui/components/PulseTabRow.kt` once
Insights needed the identical pattern for its own 4-tab restructure.

## 2026-08-29/31 — Card redesign: shadows, per-preset background tint, `DATA_SPARKLINE`

The most recent pass, done interactively over several rounds of feedback rather than one spec.
Requested: light mode's cards should go from accent-tinted to white-with-shadow (except the
Overview cards with a live sparkline, which should keep their exact prior look); dark mode's
cards should go grey-with-shadow; AI/curated cards should keep their own visual identity through
all of this.

What actually shipped, in the order it was tuned:

1. **`DATA` moved off `surfaceTinted` onto `colorScheme.surfaceVariant`** (white in light, a
   lighter grey step in dark — already existed as `PulseTokens.Surface.*.surfaceElevated`, just
   not previously used for card backgrounds) with a hand-applied `Modifier.shadow` and no border
   — the shadow does the job the border used to.
2. **New `DATA_SPARKLINE` style**, not a fold into `DATA` — the one explicit "keep exactly as-is"
   exception (Overview's sparkline-bearing asset cards), frozen to `DATA`'s pre-redesign
   background/border.
3. **Light-mode page `background` became computed**, not a flat shared token — `Color.kt`'s
   `background` field was reduced to a pure-white base, and `toColorScheme()` blends in 5% of the
   active preset's own `accent.primary` on top, so the page reads as faintly tinted per preset
   instead of one grey shared by all 5 light presets. Motivated by white `DATA` cards needing a
   background with enough contrast to actually show their shadow against — a near-white page made
   a white card indistinguishable from the page itself.
4. **A dark-mode-only `accentPrimary` border on `DATA` was tried, then dropped.** Tried at full
   strength, found too heavy, reduced to 35% alpha, then dropped entirely in the next round in
   favor of relying on the shadow alone in both modes.
5. **Shadow shape fixed: bottom-heavy → uniform.** The initial shadow used
   `CardDefaults.cardElevation`, which renders Android's default directional key-light cast (heavy
   at the bottom). Replaced with a hand-applied `Modifier.shadow(ambientColor, spotColor)`,
   weighting `ambientColor` well above `spotColor` to read as an even halo instead.
6. **Shadow strength iterated three times, light mode only** (dark mode was asked to stay
   untouched throughout) — elevation `3dp → 6dp → 9dp → 13dp`, `ambientColor`/`spotColor` alpha
   climbing alongside it each round, ending at 0.50/0.24 vs. dark mode's unchanged 0.24/0.10.
7. **Shadow scope widened from `DATA`-only to all three styles.** Originally only `DATA` cast a
   shadow (`SYNTHESIS`/`DATA_SPARKLINE` were meant to keep their exact prior look including "no
   shadow"); a later round asked for the shadow to apply to literally every card, overriding that
   earlier "keep as-is" exception for `SYNTHESIS`/`DATA_SPARKLINE` — their background/border still
   didn't change, only the shadow was added on top.
8. **`accentSurfaceStrong` (the `SYNTHESIS` background) darkened, then lightened, per mode
   independently.** First: 12% toward black in both modes. Then: light mode's 12% read as too
   dark once the new stronger shadow was in place, pulled back to 6%, then abandoned entirely for
   a different approach — white blended 15% toward `accent.primary` directly (a hue-tuned light
   tint, the same technique the page-background blend uses, just stronger). Dark mode's 12%
   toward-black formula was asked to stay untouched through every one of the light-mode passes.
9. **Bugs found and fixed along the way, not part of the original ask:**
   - `animateContentSize()` applied to a `PulseCard`'s own outer `modifier` (rather than its inner
     content) clips straight through the shadow's rounded corners — found on every expandable
     `SYNTHESIS` card in the app (`SynthesisHeroCard.kt`, Dashboard's Technical Briefing,
     Indicators' AI Executive Briefing, `DirectNews.kt`), all fixed the same way.
   - Pushed screens' own `TopAppBar`s (`SettingsScreen.kt`, `IndicatorHorizonsRoute.kt`,
     `WebViewScreen.kt`) were defaulting to `colorScheme.surface`, which no longer tracked the now
     per-preset-tinted `colorScheme.background` — given the same explicit `containerColor`
     override `NewsRoute.kt`/`AssetDetailRoute.kt` already used correctly.
   - `MainActivity`'s hardcoded `initialValue = MarketPulseTheme.LILAC` for the first Compose
     frame was flashing LILAC's dark chrome for a moment before the real persisted theme (often
     light) loaded from DataStore — fixed with a synchronous `runBlocking` read before
     `setContent`.
   - `ThemePickerGrid` was `.chunked(2)`-ing the raw preset list, which only accidentally paired
     light-with-dark per row because 5 light + 5 dark chunks into pairs; with an odd count on
     either side it degrades into solid-light-then-solid-dark rows. Fixed by splitting on
     `.isDark` first.

Net effect on the two-style framing this doc's earlier chapters describe: it's now three styles
(`DATA`/`SYNTHESIS`/`DATA_SPARKLINE`), and the "flat, shadow-less by convention" rule is retired —
see `@docs/theming-system/theming-spec.md` §6 for the current, settled state.
