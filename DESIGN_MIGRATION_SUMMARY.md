# Design Migration Summary — `feat-design-migration`

A running log of the visual-design work done on this branch, in the order it happened. Written to hand off context (e.g. to a fresh Claude Chat session) without needing the full commit history or conversation transcript.

## 1. Color system rebuilt from scratch — 10 theme presets

The old single-theme palette (`PulseBlue`, `PulseBlack`, `PulseGold`, `PulseOrange`, `AlertRed`, `PulseStatusColors`, the legacy `ColorGreen`/`ColorRed`/`ColorNeutral` trio) was deleted entirely, not deprecated. In its place: `PulseTokens` (`ui/theme/Color.kt`) defines three layers —

- **Signal** — locked per mode (light/dark), identical across every preset. Bullish/bearish/neutral/warning text + pill colors. Never themeable, so a signal reading can never be confused with a themed accent.
- **Surface** — also locked per mode. The shared neutral ramp: background, surface, elevated surface, on-surface text, outline.
- **Accent** — the one thing that varies. 10 presets, 5 values each (`primary`, `on`, `surface`, `surfaceBorder`, `tinted`): 5 light presets (Plum, Navy, Fuchsia, Graphite, Teal), 5 dark (Lilac, Sky, Sand, Rose, Aqua).

`MarketPulseTheme.kt` resolves a preset into `PulseColors` (read via `LocalPulseColors.current`) and a standard M3 `ColorScheme`. Two extended tokens are computed at runtime via `lerp()`, not hand-picked hex, specifically so future "make this a bit more/less pronounced" tweaks are one-line factor changes instead of re-deriving 10 hex values by hand:

- `surfaceTinted = lerp(accent.tinted, accent.surface, 0.45f)` — background for plain data cards (Equities, Indicators, VIX, Fear & Greed, Put/Call).
- `accentSurfaceStrong = lerp(accent.surface, accent.surfaceBorder, 0.42f)` — background for AI/curated-content cards (briefings, news, verdicts), deliberately more saturated so synthesis content reads as visually distinct from raw price data.

A Settings screen (new) hosts the preset picker; selection persists via DataStore, default is `LILAC`.

## 2. Card system consolidated — `PulseCard`

Every content card across the app now renders through one shared composable, `PulseCard(style: PulseCardStyle)` (`ui/components/PulseCard.kt`), replacing hand-rolled `Card(colors=…, border=…, shape=…)` at each call site. Two styles:

- **`DATA`** — `surfaceTinted` background, `accentSurfaceBorder` 1dp stroke, 16dp corners. Equities' `AssetCard`, Indicators' `UniversalMetricCard`, VIX, Fear & Greed, Put/Call all share this — including the stroke that was originally VIX-only, now applied to Equities too.
- **`SYNTHESIS`** — `accentSurfaceStrong` background, same stroke, 12dp corners. AI briefing, news (both card variants), weekly playbook events, tail risk, NAAIM/dark pool/net liquidity, and the summary screen's lead story/macro/domino/outlook/action-footer/verdict cards.

A third style (`NEUTRAL`, plain elevated background) existed briefly for VIX/Fear & Greed/Put-Call before the decision to fold those into `DATA` alongside Equities.

> **2026-08-16 update:** `SYNTHESIS`'s card list above reflects this branch's state, not current `main`. Building Stock Analysis end-to-end found `SYNTHESIS`/`DATA` read as nearly indistinguishable on several presets once most cards on a screen wore the "AI" tint — it stopped signaling anything. `SYNTHESIS` is now narrowed to exactly 3 cards app-wide (Summary's `VerdictCard`, Indicators' AI Executive Briefing, Dashboard's Technical Briefing — one hero card per screen); every other card listed above, including all of News and the summary-screen list, moved to `DATA`. `accentSurfaceStrong`'s formula also changed — it now blends from `accent.tinted` (the same base `surfaceTinted` uses) toward `accent.surfaceBorder` at 0.55, not from `accent.surface` toward `accent.surfaceBorder` at 0.42. Full detail: `STYLE_SPEC.md` §5–§6, `ADR-2026-08-16-stock-analysis-post-spec-refinements.md`.

Deliberately left outside this system: `HorizonNavigationCard` (a CTA button, not a content card), `UnifiedScoreHeaderCard`/`UniversalGaugeCard` (background is signal-owned, not accent-owned), `PresetSwatchCard` (must render a different preset's raw colors on purpose), the sector heatmap tiles.

## 3. Pill system consolidated — `SignalPill` / `DirectionalChangePill`

`SignalPill` (`ui/components/widgets/SignalPill.kt`) is now the single implementation behind every small colored badge — sentiment tags, impact levels, regime/status pills. Text color is computed at render time, not baked into the signal token itself:

- Light mode: pill text blends 20% toward black.
- Dark mode: pill text blends 40% toward white.
- Mode detected via `colorScheme.background.luminance() > 0.5f`.

`DirectionalChangePill` wraps it for price/ratio changes: a filled triangle (up/down) replaces the old hollow-chevron + `+`/`-` prefix, text is now unsigned magnitude only (the triangle already carries the sign), and a third `FLAT` state (flat-bar icon, neutral tone) handles an exact 0.0% reading instead of forcing it into up or down.

## 4. Text-color audit across cards

A pass through Insights/Summary/Dashboard cards found several places reading `colorScheme.secondary` / `onSurfaceVariant` for titles and body text where it silently rendered as muted grey instead of full-strength — a leftover habit from before the token system existed. Fixed rule now: card titles and body/explainer text are always `onSurface` (dark-on-light / white-on-dark), with two carve-outs — genuine timestamp strings ("Analyzed as of…") and text with an intentional signal/condition color. Touched: `WeeklyPlaybookView.kt` (event titles, "Market Context" label), `MarketRisksView.kt`/`MarketPostureView.kt` (card titles, NAAIM/dark pool/net liquidity labels), Indicators card titles (e.g. "Fear & Greed Index").

## 5. Typography consolidated — two title tiers

Card titles across the app were an inconsistent mix of 17sp-semibold, 17sp-bold, and 15sp-bold depending on which card you looked at. Standardized to two deliberate tiers:

- **`DATA`-style cards** (Equities, VIX, Indicators) — `titleMedium.copy(fontWeight = Bold)`, 17sp bold. Unchanged — this is the larger, "raw number" tier.
- **`SYNTHESIS`-style cards** (everything AI/curated) — `titleSmall`, 15sp semi-bold, no weight override. Applied to `NewsArticleCard`, `NewsPreviewCard`, `LeadStoryCard`, `MacroCard`, `DominoCard` (which had a second, separate `fontWeight = Bold` parameter hiding on the `Text` call itself, not just in `style=`), `OutlookCard`, `ActionFooter`, `WeeklyEventCard`, `TailRiskCard`, `NaaimExposureCard`, `DarkPoolCard`, `NetLiquidityCard`.

## 6. Top bar / status bar behavior

- Child screens with their own back-button chrome (News, Settings, WebView routes) no longer render the global `AppTopBar` at all — `hasOwnTopBar` check in `MainActivity.kt`.
- The top bar now shows the current screen's actual title instead of a static "MarketPulse."
- Screen content gets dynamic top padding (`innerPadding.calculateTopPadding() + scrollBehavior.state.heightOffset.toDp()`, coerced ≥ 0) so it doesn't sit underneath the collapsing bar.
- Top bar and status bar now blend with the page background and fade to transparent together as the bar collapses on scroll-down, and reappear together on scroll-up. This required two separate fixes: `TopAppBar`'s own `windowInsets` reserve status-bar space regardless of collapse state by design (M3 source-verified), worked around by zeroing them on the inner `TopAppBar` and taking over that region with a separate scrim `Box`; and a `Modifier.background(color).alpha(x)` ordering bug where `alpha` was applied after `background`, so the fade never took effect until reordered to `.alpha(x).background(color)`.
- The legacy `android:statusBarColor` override in `themes.xml`/`values-night/themes.xml` — a static opaque color left over from the project template — was fighting `enableEdgeToEdge()` and has been removed.
- Bottom nav bar gets more start/end margin and reduced bottom padding.

## 7. Everything-else pass

- Removed the redundant `+`/`-` sign prefix from percentage-change text now that filled up/down triangles carry the sign; added a neutral-toned flat state for exactly 0.0%.
- News card title text switched to a darker/near-black tone with a darker card background than equities cards, and padding tightened, matching a reference screenshot the design was checked against.
- Pill contrast retuned per-mode (see §3) after a side-by-side light/dark comparison.

## Not yet done

- None of this has been committed to git yet.
- `StockAnalysisRoute`/Screen (the Analysis tab detail views) remains a placeholder pending a separate design pass.
- Open token/contrast items are tracked in `STYLE_SPEC.md §14` rather than duplicated here (an unresolved `signal.unknown` color, unbundled type weights, a handful of un-tokenized corner-radius/spacing literals, unverified WCAG contrast on the two blend factors in §1).
