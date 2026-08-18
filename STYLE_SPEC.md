# Market Pulse — Current Style Spec

Design reference extracted from source. Every color, type, spacing, and shape value defined in the Android client today, read straight out of `Color.kt`, `MarketPulseTheme.kt`, `PulseColors.kt`, `PulseCard.kt`, `SignalPill.kt`, `Type.kt`, and `dimens.xml` — plus the inconsistencies worth a decision.

**This documents what exists, not what's recommended.**

- Package: `com.marketlabs.pulse`
- Stack: Kotlin · Jetpack Compose · Material 3
- Snapshot: 2026-08-16 · branch `feat-stock-analysis`

**2026-08-10 snapshot:** the entire color system was rebuilt: the old flat single-theme palette (`PulseBlue`/`PulseBlack`/`PulseGold`/`PulseOrange`/`AlertRed`, `PulseStatusColors`, `ColorGreen`/`ColorRed`/`ColorNeutral`) is gone — not deprecated, deleted — replaced by a 10-preset system described below. Most of §1–§3 in the version of this doc before that no longer applied to anything in the running app.

**Since that snapshot:** building Stock Analysis end-to-end put real, dense card content against these tokens for the first time, and surfaced three more fixes — this time verified with actual contrast math per preset instead of eyeballed. `Accent.*.tinted` was desaturated ~50% across all 10 presets (§4) — it read too saturated once real content sat on top of it. `onSurfaceMuted` (§3) had its lightness shifted for contrast — it's the one shared grey behind dates, stat labels, and every muted subtitle app-wide. `accentSurfaceStrong` (§5) was reformulated to derive from `tinted` too, at a stronger blend, instead of an unrelated `surface`/`surfaceBorder` computation. Separately — a UI decision, not a token one — `PulseCardStyle.SYNTHESIS` itself was narrowed from roughly two dozen cards down to exactly 3 app-wide (§6): most cards that used to read as "curated/AI content" now read as plain `DATA` instead, since having most cards on a screen wear the accent tint had stopped meaning anything.

---

## 1. Theme architecture — 10 presets, three token layers

The app supports **10 baked-mode presets** — 5 light (Plum, Navy, Fuchsia, Graphite, Teal), 5 dark (Lilac, Sky, Sand, Rose, Aqua). Picking a preset **is** the appearance setting — there's no separate "follow system" toggle, and a chosen preset overrides whatever light/dark mode the OS is in. Selection persists via DataStore (`ThemeRepository`), default is `LILAC`.

Every preset resolves from the same three data sources in `PulseTokens` (`ui/theme/Color.kt`):

| Layer | Varies by preset? | What it covers |
|---|---|---|
| **Signal** | No — locked per mode | Bullish/bearish/neutral/warning meaning. Every light preset paints the same 8 hexes; every dark preset paints the same (different) 8 hexes. `Accent` is never allowed to override these — a themed accent can never be mistaken for a market read. |
| **Surface** | No — locked per mode | The shared neutral ramp (background, surface, elevated surface, outline, on-surface text). One instance for all 5 light presets, one for all 5 dark presets. |
| **Accent** | **Yes — the one thing that changes** | 5 values per preset: `primary` (the preset's brand color), `on` (text/icon color sitting on top of it), `surface` + `surfaceBorder` (a soft tint + matching hairline border for AI/curated cards), `tinted` (an accent-washed neutral for price cards). |

Resolution happens once per composition, in `MarketPulseTheme.toPulseColors()` / `.toColorScheme()` (`ui/theme/MarketPulseTheme.kt`) — every screen reads either `MaterialTheme.colorScheme` (standard M3 roles) or `LocalPulseColors.current` (the extended tokens below), never `PulseTokens` directly.

---

## 2. Signal tokens (locked)

The one color category every preset shares. `PulseTokens.Signal` in `Color.kt`.

| Role | Light text | Light pill | Dark text | Dark pill |
|---|---|---|---|---|
| Bullish | `#1B5E20` | `#C6E4C1` | `#81C784` | `#1F4A28` |
| Bearish | `#B71C1C` | `#F8C8BE` | `#F37B7B` | `#4A2222` |
| Neutral | `#B36A00` | `#F0DEB0` | `#FFD54F` | `#4A3820` |
| Warning | `#BF360C` | `#F5CFB8` | `#E65100` | `#4A2210` |

`signal.unknown` (the fourth state, for missing data) has **no resolved hex yet** — placeholder-mapped to the mode's own `onSurfaceMuted` until Design provides one.

Access pattern: never branch on the `SignalColor` enum by hand. Use `SignalColor.textColor` / `.pillColor` (`ui/theme/SignalColorExtensions.kt`), which read `LocalPulseColors.current` under the hood.

---

## 3. Surface ramp (locked per mode)

Shared neutral scale — one instance for all 5 light presets, one for all 5 dark. `PulseTokens.Surface` in `Color.kt`.

| Role | Light | Dark |
|---|---|---|
| `background` | `#F8F6F2` | `#0D0E12` |
| `surface` | `#FBFAF7` | `#17181D` |
| `surfaceElevated` | `#FFFFFF` | `#1F2026` |
| `onBackground` / `onSurface` | `#14161B` | `#F0EEF3` |
| `onSurfaceMuted` | `#585A61` | `#AFB0B6` |
| `outline` | `#E4E2DC` | `#2A2B31` |

`background` (light mode) was hand-tuned once already — nudged lighter (was `#F4F2ED`) after the global top bar started sharing this exact color, so it needed to read as a clean "greyish-white," not visibly cream. Dark mode's background was untouched.

`onSurfaceMuted` was hand-tuned a second time (2026-08-16): darkened ~8% in light mode, lightened ~8% in dark mode (HLS, lightness only), after this token's contrast against real card backgrounds was measured — not eyeballed — across all 10 presets while building Stock Analysis. It's the single highest-traffic token in this doc: 130+ call sites app-wide via `onSurfaceMuted`/`colorScheme.onSurfaceVariant`, which resolve to the same value (§5).

---

## 4. Accent tokens — all 10 presets

The one layer that actually differs per preset. `PulseTokens.Accent` in `Color.kt`. `tinted` is the *literal* per-preset value — see §5 for what price cards actually render (a runtime blend, not this raw hex).

| Preset | Mode | `primary` | `on` | `surface` | `surfaceBorder` | `tinted` (raw) |
|---|---|---|---|---|---|---|
| Plum | Light | `#5B2A82` | `#FFFFFF` | `#EBDFF3` | `#D8C3E4` | `#F2F0F4` |
| Navy | Light | `#14315E` | `#FFFFFF` | `#E3E9F3` | `#C7D3E5` | `#EFF0F3` |
| Fuchsia | Light | `#9C1A6B` | `#FFFFFF` | `#F5E1EE` | `#ECC7DE` | `#F3EDF0` |
| Graphite | Light | `#2B303A` | `#FFFFFF` | `#E9E9EB` | `#DDDCD8` | `#EEEDEB` |
| Teal | Light | `#05555C` | `#FFFFFF` | `#DDECED` | `#C1DEDE` | `#ECEFEF` |
| Lilac | Dark | `#C7A9FF` | `#1A0F2E` | `#2C2338` | `#3B2E4B` | `#201E24` |
| Sky | Dark | `#7BC0FF` | `#08192E` | `#1E2A3B` | `#2A3B54` | `#1A1D22` |
| Sand | Dark | `#C9B49A` | `#1F1A11` | `#2B261E` | `#3C3325` | `#1C1B19` |
| Rose | Dark | `#E9A2D8` | `#2B0F22` | `#331F2C` | `#452838` | `#1D1B1D` |
| Aqua | Dark | `#7ED9D6` | `#062120` | `#1B2E2D` | `#294241` | `#161B1B` |

`tinted` was desaturated ~50% (2026-08-16, HLS, saturation only — hue and lightness untouched) across all 10 rows above. It read too saturated once real card content sat on top of it. `primary`/`on`/`surface`/`surfaceBorder` are untouched.

---

## 5. Derived tokens — the actual theme ↔ color relationship

Two extended tokens on `PulseColors` (read via `LocalPulseColors.current`) aren't stored as constants anywhere — they're **computed at runtime, once per preset resolution**, as a blend between two of the accent values above. This is deliberate: three separate sessions of "make this background a bit more/less pronounced" turned into single-line factor edits instead of hand-recomputing 10 hex values each time.

| Token | Formula | Blend factor | Used by |
|---|---|---|---|
| `surfaceTinted` | `lerp(accent.tinted, accent.surface, f)` | **0.45** | Every `PulseCard(style = DATA)` — the large majority of cards app-wide as of 2026-08-16 (§6). |
| `accentSurfaceStrong` | `lerp(accent.tinted, accent.surfaceBorder, f)` | **0.55** | Every `PulseCard(style = SYNTHESIS)` — now exactly 3 cards app-wide (§6). |

Both blend *toward* an already-defined, already-vetted value in the same hue family (not toward an arbitrary new hex), so the result stays visually coherent with the preset regardless of which direction a future tweak needs to go. `accentSurfaceStrong` is intentionally the more saturated of the two — curated/AI content is meant to read as visibly "more accented" than a plain data card sitting next to it, not just a different token name.

`accentSurfaceStrong`'s formula changed 2026-08-16: it used to blend `accent.surface` toward `accent.surfaceBorder` (0.42), entirely independent of `tinted`. It now starts from the same `accent.tinted` base `surfaceTinted` does, blended toward the stronger `surfaceBorder` anchor at a higher factor (0.55 vs. `surfaceTinted`'s 0.45) — so the two `PulseCard` styles read as the same underlying color family at two intensities, rather than two unrelated computations that happened to land in the same hue.

`colorScheme.primary`/`onPrimary` also mirror `accentPrimary`/`accentOn` exactly (not a second source of truth) — purely so built-in M3 components (ripples, default `Switch` tinting) that only know how to read `colorScheme.*` stay coherent with the accent without individual migration. App code should always read `LocalPulseColors.current`, never `MaterialTheme.colorScheme` for anything this token system defines.

`colorScheme.secondary`/`secondaryContainer` have no independent hex — resolved as `onSurfaceMuted`/`surfaceElevated`. **Known trap:** several card titles were found this session still reading `colorScheme.secondary` directly (a leftover pre-migration habit) instead of `onSurface` — they rendered muted instead of full-strength text. All fixed, but worth flagging as an easy mistake to reintroduce.

`error`/`errorContainer`/`onError`/`onErrorContainer` have **no resolved hex in this system at all** — left out of the `ColorScheme` builder calls entirely so M3's own baseline default applies, same reasoning as `signal.unknown`.

---

## 6. Card system — `PulseCard`

Every content card in the app now goes through one shared composable, `PulseCard(style: PulseCardStyle)` (`ui/components/PulseCard.kt`), instead of hand-rolled `Card(colors=…, border=…, shape=…)` per call site. Exactly **two** styles:

| Style | Background | Border | Corner radius | Used by |
|---|---|---|---|---|
| `DATA` | `surfaceTinted` | `accentSurfaceBorder`, 1dp | `corner_radius_card_large` (16dp) | Everything not in the `SYNTHESIS` row — Equities' `AssetCard`, Indicators' `UniversalMetricCard`, VIX, Fear & Greed, Put/Call, News (both card variants), Summary's Lead Story/Macro/Domino/Outlook/Action Footer, Insights' Weekly Playbook/Institutional Posture/Market Risks cards, and Stock Analysis' list card + `TechnicalRead`/Deep Study/Scenarios/Direct News |
| `SYNTHESIS` | `accentSurfaceStrong` | `accentSurfaceBorder`, 1dp | `corner_radius_card` (12dp) | **Narrowed 2026-08-16 to exactly 3 cards app-wide:** Summary's `VerdictCard`, Indicators' AI Executive Briefing, Dashboard's Technical Briefing — one AI-conclusion hero card per screen |

**2026-08-16:** `SYNTHESIS` used to cover roughly two dozen cards (Technical Briefing, News, Weekly Playbook events, Tail Risk, NAAIM/Dark Pool/Net Liquidity, Lead Story/Macro/Domino/Outlook/Action Footer, Verdict, and Stock Analysis' own list card + `TechnicalRead`/Deep Study/Scenarios/Direct News). A contrast pass across all 10 presets found the two styles read as nearly indistinguishable on several of them, and with most cards on a screen wearing the "AI-authored" tint, it stopped signaling anything — it just meant "this app has two shades of card." Narrowed to one hero card per screen; everything else moved to `DATA`.

A third style, `NEUTRAL` (plain white/elevated background, no accent wash — used briefly for VIX/Fear & Greed/Put-Call on the theory a computed reading shouldn't look like raw price data), existed and was retired once that distinction stopped being wanted; those cards moved onto `DATA`.

**Deliberately excluded** from this system (still plain `Card`, on purpose): `HorizonNavigationCard` (a pill-shaped CTA button, not a content card), `UnifiedScoreHeaderCard` / `UniversalGaugeCard` (background is signal-colored, passed in by the caller — showing raw computed data, not an AI's interpretation of it), `PresetSwatchCard` (must render a *different* preset's raw colors regardless of the active theme, so it can't read `LocalPulseColors.current`), the sector rotation heatmap tiles (the one place a signal color is allowed to own an entire tile background).

---

## 7. Signal pill system — `SignalPill`

Every small colored badge — sentiment tags, impact levels, status pills, regime pills, the directional change indicators — goes through one shared composable, `SignalPill` (`ui/components/widgets/SignalPill.kt`).

- Background: whichever `signal.*.pill` (or `accentSurfaceBorder`-adjacent) color the caller passes in.
- Padding: `padding_medium` (8dp) horizontal / `padding_small` (4dp) vertical, `corner_radius_pill` (fully stadium-shaped).
- **Text color is blended from the signal token, direction depends on mode** — this is computed inside `SignalPill`, not baked into `signal.*.text` itself, since those tokens are also used as plain standalone text elsewhere (e.g. VIX's own "GREED"/"FEAR" label) where the full-strength color is correct:
  - Light mode: blend toward **black**, 20%. (Light-mode signal text is already dark-on-light; blending toward white read as too soft against the pill's own light fill.)
  - Dark mode: blend toward **white**, currently 40%. (Dark-mode signal text starts lighter already, so this pushes it further toward a bright, high-contrast label.)
  - Detected via `colorScheme.background.luminance() > 0.5f` — no new "isDark" token, reuses what's already there.
- `leadingIcon` is an optional slot, so the same component backs plain text pills and icon+text ones (`DirectionalChangePill`'s triangle) without duplicating the implementation.

`DirectionalChangePill` (the up/down-triangle % pills used throughout for price/ratio changes) is a thin wrapper over `SignalPill`. Notable behavior: text is unsigned magnitude only (the triangle already states the sign — `"2.41%"`, never `"+2.41%"`), and there's a third `FLAT` state (a flat-bar icon + neutral tone) for an exact 0% reading, rather than forcing it into an arbitrary up or down.

---

## 8. Typography scale

Unchanged from the previous snapshot — `AppTypography` in `Type.kt` — **except** a new convention for which size a card title uses.

| Role | Family / weight | Size / line-height / tracking | Bundled? |
|---|---|---|---|
| `headlineLarge` | Montserrat ExtraBold (800) | 32sp / 40sp / −0.5sp | ✅ |
| `headlineMedium` | Montserrat Bold (700) | 28sp / 36sp | ✅ |
| `headlineSmall` | Montserrat Bold (700) | 24sp / 32sp | ✅ |
| `titleLarge` | Montserrat Bold (700) | 20sp / 28sp | ✅ |
| `titleMedium` | Montserrat SemiBold (600) | 17sp / 24sp / 0.15sp | ❌ no file |
| `titleSmall` | Montserrat SemiBold (600) | 15sp / 20sp / 0.1sp | ❌ no file |
| `bodyLarge` | Montserrat Normal (400) | 15sp / 24sp / 0.5sp | ❌ no file |
| `bodyMedium` | Montserrat Normal (400) | 14sp / 20sp / 0.25sp | ❌ no file |
| `bodySmall` | Montserrat Normal (400) | 12sp / 16sp / 0.4sp | ❌ no file |
| `labelLarge` | Inter Bold (700) | 14sp / 20sp / 0.1sp | ❌ no file |
| `labelMedium` | Inter SemiBold (600) | 12sp / 16sp / 0.5sp | ❌ no file |
| `labelSmall` | Inter SemiBold (600) | 11sp / 16sp / 1.2sp — "terminal aesthetic" per code comment | ❌ no file |

**8 of 12 styles still request a weight that isn't bundled** — unaddressed this round.

| Family | Bundled weights | Requested but missing |
|---|---|---|
| Montserrat | Bold, ExtraBold, Medium | Normal, SemiBold |
| Inter | Regular, Medium | Bold, SemiBold |

### Card title convention (new)

Two tiers, by card style (§6):

- **`DATA`-style card titles** — `titleMedium.copy(fontWeight = Bold)`, **17sp bold**. Equities' `AssetCard`, VIX, Indicators' `UniversalMetricCard`.
- **`SYNTHESIS`-style card titles** — `titleSmall`, **15sp semi-bold, no weight override**. Every AI/curated-content card. Was previously an inconsistent mix of 17sp-semibold, 17sp-bold, and 15sp-bold across different cards (nobody had applied the same override twice) — standardized in this pass. One card (`DominoCard`) had a `fontWeight = Bold` parameter set directly on the `Text` composable, separate from its `style=`, invisible from a glance at the style line alone — worth checking for that pattern specifically if an inconsistency like this shows up again.

### Card body-text color convention (new)

Every card's inner subtitle/explainer/description text is `onSurface`, matching its title — **not** `onSurfaceVariant`/`colorScheme.secondary`. The two carve-outs: genuine date/timestamp strings ("Analyzed as of…", "Released…"), and text with an intentional signal/condition color (impact badges, status pills). A muted role was found applied well past just those two cases before this pass — mostly the same `colorScheme.secondary`-reads-as-muted trap noted in §5.

---

## 9. Spacing scale

Unchanged from the previous snapshot.

| Token | Value |
|---|---|
| `padding_tiny` | 2dp |
| `padding_micro` | 3dp ⚠️ |
| `padding_small` | 4dp |
| `padding_medium` | 8dp |
| `padding_standard` | 12dp |
| `padding_large` | 16dp |
| `padding_xlarge` | 20dp |
| `padding_xxlarge` | 24dp |
| `padding_extra_large` | 24dp ⚠️ (duplicate of `padding_xxlarge`) |

`padding_tiny` (2dp) and `padding_micro` (3dp) are still one unit apart, still likely redundant. Neither collision was addressed this round.

---

## 10. Corner radius

Same finding as the previous snapshot, re-verified: a fresh grep still finds the exact same three hardcoded literal values at composable call sites instead of the token — `6.dp` (×4), `8.dp` (×1), `12.dp` (×4) — against 20 call sites that do correctly reference a `dimensionResource`. The token scale is more load-bearing now than before (`PulseCard` and `SignalPill` both centralize their own shape choice through it), but the remaining ad-hoc literals weren't swept up.

| Token | Value |
|---|---|
| `corner_radius_chip` | 6dp |
| `corner_radius_small` | 8dp |
| `corner_radius_card` | 12dp — now `PulseCard`'s `SYNTHESIS` default |
| `corner_radius_card_large` | 16dp ⚠️ — now `PulseCard`'s `DATA` default |
| `vix_corner_radius` | 16dp ⚠️ (duplicate of `corner_radius_card_large`) |
| `corner_radius_card_extra_large` | 24dp |
| `corner_radius_pill` | 50dp — now `SignalPill`'s and `FloatingBottomNav`'s shape; far more central to the app's look than when this was first documented |

---

## 11. Borders, elevation & icon sizes

Still effectively no shadow system — one exception now exists on purpose (see below), and it's the only one.

| Token | Value |
|---|---|
| `border_thin` | 1dp — now `PulseCard`'s and `SignalPill`-adjacent border weight everywhere |
| `border_medium` | 3dp |
| `border_thick` | 6dp |
| `icon_size_small` | 16dp |
| `icon_size_medium` | 20dp |
| `icon_size_large` | 24dp |
| `bullet_size` | 6dp |
| `nav_elevation` | 2dp — **new.** `FloatingBottomNav`'s shadow, a deliberate, explicitly flagged one-off exception to the flat/no-shadow rule (documented in `CLAUDE.md`). No blur — minSdk 26 has no `RenderEffect` to build one from, so it's a solid `shadowElevation`, not a soft glow. |

### Component-specific dimensions

Unchanged.

| Token | Value |
|---|---|
| `timeline_dot_size` | 10dp |
| `timeline_node_width` | 20dp |
| `timeline_label_width` | 64dp |
| `gauge_size` / `gauge_size_large` | 160dp |
| `gauge_stroke_width` | 10dp |
| `gauge_needle_width` / `_radius` / `_overhang` | 3dp / 4dp / 6dp |
| `bar_tick_overhang` | 4dp |
| `progress_bar_width` / `_large` | 80dp / 120dp |
| `progress_bar_height` | 8dp |
| `tab_indicator_width` / `_height` / `_corner` | 40dp / 4dp / 4dp |

---

## 12. Opacity in practice

Re-counted fresh against the current codebase (was last counted before the migration; values shifted meaningfully since a lot of the old alpha-based "fake pill background" pattern — `statusColor.copy(alpha = 0.15f)` standing in for a real pill token — was found and replaced with real `signal.*.pill` tokens this session).

| Alpha | Occurrences |
|---|---|
| 0.10 | ×19 |
| 0.50 | ×10 |
| 0.40 | ×9 |
| 0.30 | ×8 |
| 0.20 | ×5 |
| 0.80 | ×4 |
| 0.15 | ×4 |
| 0.05 | ×3 |
| 0.70 | ×1 |
| 0.0 | ×1 |

Still no named opacity scale — every `.copy(alpha = …)` call still picks its own float independently. Not addressed this round.

---

## 13. Vestigial assets

- `res/values/themes.xml` / `res/values-night/themes.xml` — **partially cleaned up.** The `android:statusBarColor` override (a static opaque color left over from the original Android Studio template) was actively fighting `enableEdgeToEdge()`'s attempt to make the status bar transparent, and has been removed. The rest of the boilerplate — `colorPrimary = purple_500`, `colorSecondary = teal_200`, etc. — is still there, still unused by Compose's own `MaterialTheme()`, still just launch-time scaffolding. Left alone since it's cosmetic and out of scope for whatever prompted the statusBarColor fix.
- `fab_margin` / `fragment_horizontal_margin` in the width-qualified `dimens.xml` files — still present, still unused-looking template boilerplate. Not investigated this round.

---

## 14. Decisions — status update

The nine items from the previous snapshot, with what's actually happened to each:

1. ~~**Light-mode primary/onPrimary is ~2:1 contrast.**~~ **Resolved.** Every preset's `accent.primary`/`accent.on` pair was authored together, not derived from an unrelated brand-blue + black-text combination.
2. ~~**Light-mode surfaces sit within ~3% luminance of each other.**~~ **Resolved.** The new surface ramp (`background` → `surface` → `surfaceElevated`) plus the `surfaceTinted`/`accentSurfaceStrong` derived tokens (§5) give real, intentional separation between page background, plain cards, and curated-content cards.
3. ~~**`secondary` changes role between themes.**~~ **Resolved architecturally, still a live trap in practice.** `colorScheme.secondary` now has one consistent meaning (`onSurfaceMuted`) in every preset — but several call sites were still reading it directly for card titles this session, where it silently rendered muted instead of full-strength. Worth a dedicated grep sweep (`MaterialTheme.colorScheme.secondary` outside of genuinely-meant-to-be-muted contexts) if this hasn't been done exhaustively.
4. **8 of 12 type styles request an unbundled weight.** Still open, untouched.
5. **Two spacing tokens collide.** Still open (§9).
6. **Corner radius tokens aren't the full source of truth.** Improved in the sense that `PulseCard`/`SignalPill` now centralize the majority of shape usage through the token system, but the same 9 literal-`dp` call sites from the original audit are still there (§10).
7. **No formal opacity scale.** Still open (§12).
8. ~~**Legacy flat status colors** (`ColorGreen`/`ColorRed`/`ColorNeutral`).~~ **Resolved.** Deleted entirely as part of the migration, not just superseded.
9. **`themes.xml` still ships default Material purple/teal.** **Partially resolved** — the one actively-harmful part (`statusBarColor` fighting edge-to-edge) is fixed; the cosmetic remainder is still there (§13).

New items worth a decision, surfaced by this round of work:

10. **`signal.unknown` still has no resolved hex.** Placeholder-mapped to `onSurfaceMuted` since the very first pass of the migration. Needs a real value from Design before the data-missing state can look intentional rather than "we forgot this."
11. ~~**`accentSurfaceStrong`'s 0.42 and `surfaceTinted`'s 0.45 blend factors were tuned by eye, not against a contrast target.**~~ **Resolved 2026-08-16.** Both formulas were checked against measured contrast ratios per preset while building Stock Analysis (not exhaustively against both text colors sitting on top of them, but against the card-background-vs-page-background pairing that motivated the original concern) — `accentSurfaceStrong` was reformulated as a result (§5), `surfaceTinted`'s 0.45 was left as-is since it measured adequately.
12. **`SignalPill`'s dark-mode text blend (40% toward white) was tuned live, mid-session, well past the initial 12–15% estimate.** Worth a second look against real content on a device — a jump that large usually means the original blend was under-corrected, but it's also possible 40% overshoots for some of the 5 dark presets specifically. Not touched this round.
13. **`onSurfaceMuted`'s new 2026-08-16 lightness values (§3) haven't been re-checked against `SignalPill`'s dark-mode text-blend logic (item 12) or against `signal.unknown`'s placeholder mapping (item 10), both of which read `onSurfaceMuted` directly.** Worth confirming neither drifted out of the range that made sense when they were last tuned.

---

*Sourced from `ui/theme/Color.kt`, `MarketPulseTheme.kt`, `PulseColors.kt`, `Type.kt`, `ui/components/PulseCard.kt`, `ui/components/widgets/SignalPill.kt`, `res/values{,-w600dp,-w936dp}/dimens.xml`, `res/values{,-night}/themes.xml`, and fresh grep sweeps of `ui/` for shape, elevation, border, and alpha usage.*
