# Market Pulse — Current Style Spec

Design reference extracted from source. Every color, type, spacing, and shape value defined in the Android client today, read straight out of `Color.kt`, `Theme.kt`, `Type.kt`, and `dimens.xml` — plus the inconsistencies worth a decision before the revamp starts.

**This documents what exists, not what's recommended.**

- Package: `com.marketlabs.pulse`
- Stack: Kotlin · Jetpack Compose · Material 3
- Snapshot: 2026-08-08 · branch `feat-stock-analysis`

---

## 1. Brand marks

Two dedicated brand colors are defined. Everything else derives from Material role mapping or the semantic status colors below — there is no broader brand palette to draw from yet.

| Name | Hex | Variable |
|---|---|---|
| Pulse Black | `#000000` | `PulseBlack` |
| Pulse Blue | `#083B95` | `PulseBlue` |
| Pulse Gold | `#F9A825` | `PulseGold` |
| Pulse Orange | `#EF6C00` | `PulseOrange` |
| Alert Red | `#C62828` | `AlertRed` |

---

## 2. Material role mapping — light vs. dark

How the five brand colors get distributed across Material 3's `ColorScheme` roles in `Theme.kt`.

| Role | Light | Dark |
|---|---|---|
| `primary` | `#083B95` ⚠️ | `#F9A825` |
| `onPrimary` | `#000000` ⚠️ | `#000000` |
| `secondary` | `#000000` | `#EF6C00` |
| `background` | `#F5F5F5` | `#000000` |
| `onBackground` | `#000000` | `#EFEFEF` |
| `surface` | `#FAFAFA` | `#121212` |
| `onSurface` | `#000000` | `#EFEFEF` |
| `surfaceVariant` | `#FFFFFF` | `#2C2C2C` |
| `error` | `#C62828` | `#C62828` |

⚠️ **Contrast issue:** light-mode `primary` (`#083B95`) with `onPrimary` (`#000000`) comes out to roughly **2:1** contrast — well under WCAG AA's 4.5:1 minimum for text. Any filled button or chip using this pairing in light mode is likely hard to read. In dark mode, `onPrimary` is also black, but it sits on gold (`#F9A825`), which is legible — it's specifically the light-mode pairing that's broken.

Also worth a decision:
- `secondary` means something different per theme — neutral black in light mode, a bright accent orange in dark mode.
- Light-mode `background` (#F5F5F5) → `surface` (#FAFAFA) → `surfaceVariant` (#FFFFFF) span under 3% luminance — see [§9](#9-vestigial-assets) and the gaps list.

---

## 3. Semantic status colors

`PulseStatusColors` is the one place colors are already theme-aware and centralized — this is the pattern the rest of the palette should probably follow.

| Signal | Light text | Light bg | Dark text | Dark bg |
|---|---|---|---|---|
| Bullish | `#2E7D32` | `#BADCBE` | `#81C784` | `#1B5E20` @25% |
| Bearish | `#C62828` | `#F3D8DB` | `#F37B7B` | `#B71C1C` @25% |
| Neutral | `#D87B00` | `#F5E3CD` | `#FFD54F` | `#F57F17` @15% |
| Warning | `#CC4A00` | `#F8DFD3` | `#E65100` | `#E65100` @15% |

A separate, older trio — `ColorGreen` (`#2E7D32`), `ColorRed` (`#C62828`), `ColorNeutral` (`#CE5A03`) — duplicates the light-mode text colors above but isn't theme-aware and doesn't appear to route through `SignalColor`. Likely a leftover from before `PulseStatusColors` existed.

---

## 4. Typography scale

All 12 Material 3 type roles from `AppTypography`. Only five `.ttf` files ship: Montserrat Bold/ExtraBold/Medium, and Inter Regular/Medium.

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

**8 of 12 styles request a weight that isn't bundled.** Compose falls back to the nearest bundled weight instead of the one `Type.kt` specifies. In practice, most body copy and every label in the app is likely rendering heavier than the type scale intends.

| Family | Bundled weights | Requested but missing |
|---|---|---|
| Montserrat | Bold, ExtraBold, Medium | Normal, SemiBold |
| Inter | Regular, Medium | Bold, SemiBold |

---

## 5. Spacing scale

All named padding tokens from `dimens.xml`.

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

`padding_tiny` (2dp) and `padding_micro` (3dp) are one unit apart — likely redundant granularity.

---

## 6. Corner radius

Six named radii, but a grep across `ui/` found only three literal values actually used at composable call sites — `6.dp` (×4), `8.dp` (×1), `12.dp` (×4) — all hardcoded rather than referencing these tokens. The scale exists in `dimens.xml` but isn't yet the source of truth in code.

| Token | Value |
|---|---|
| `corner_radius_chip` | 6dp |
| `corner_radius_small` | 8dp |
| `corner_radius_card` | 12dp |
| `corner_radius_card_large` | 16dp ⚠️ |
| `vix_corner_radius` | 16dp ⚠️ (duplicate of `corner_radius_card_large`) |
| `corner_radius_card_extra_large` | 24dp |
| `corner_radius_pill` | 50dp |

---

## 7. Borders, elevation & icon sizes

There's effectively no shadow system today — `elevation_small` (3dp) is the only elevation token defined, its one usage site (`MarketRisksView.kt`) overrides it straight to `0.dp`, and there are zero `.shadow()` calls anywhere in the codebase. Depth currently comes entirely from border weight and background contrast.

| Token | Value |
|---|---|
| `border_thin` | 1dp |
| `border_medium` | 3dp |
| `border_thick` | 6dp |
| `icon_size_small` | 16dp |
| `icon_size_medium` | 20dp |
| `icon_size_large` | 24dp |
| `bullet_size` | 6dp |

### Component-specific dimensions

Single-purpose measurements for specific widgets (the gauge, the timeline, the tab indicator) rather than a reusable scale — worth leaving as-is unless a new component needs the same shape.

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

## 8. Opacity in practice

No named opacity tokens exist anywhere — every `.copy(alpha = …)` call picks its own float. Every distinct value found across `ui/`, with occurrence count:

| Alpha | Occurrences |
|---|---|
| 0.05 | ×3 |
| 0.10 | ×22 |
| 0.15 | ×9 |
| 0.20 | ×5 |
| 0.25 | ×2 |
| 0.30 | ×7 |
| 0.40 | ×17 |
| 0.50 | ×13 |
| 0.70 | ×1 |
| 0.80 | ×6 |

Ten distinct values, none named, unevenly spaced.

---

## 9. Vestigial assets

Not part of the live Compose theme, but still shipping in the project and worth a decision:

- `res/values/themes.xml` / `res/values-night/themes.xml` — `Theme.MarketPulse` still carries the unmodified Android Studio default (`colorPrimary = purple_500`, `colorSecondary = teal_200`), wired via `android:theme` in the manifest. Compose's `MaterialTheme()` overrides it once the tree inflates, but it likely governs the window background for a brief moment at cold start.
- `fab_margin` / `fragment_horizontal_margin` in the width-qualified `dimens.xml` files read like unused template boilerplate.

---

## 10. Decisions worth making before the revamp

Not a sequence — nine independent calls, gathered here so the design doc can address each deliberately instead of inheriting it by default.

1. **Light-mode primary/onPrimary is ~2:1 contrast.** Black text on Pulse Blue (`#083B95`) fails WCAG AA (4.5:1) by a wide margin.
2. **Light-mode surfaces sit within ~3% luminance of each other.** `background` `#F5F5F5` → `surface` `#FAFAFA` → `surfaceVariant` `#FFFFFF`, combined with zero shadow usage anywhere, means card boundaries may be nearly invisible in light mode.
3. **`secondary` changes role between themes.** A neutral black in light mode, a bright accent orange in dark mode — decide whether secondary should carry one consistent meaning.
4. **8 of 12 type styles request an unbundled weight.** Either bundle the missing weights or repoint the styles at weights that exist.
5. **Two spacing tokens collide.** `padding_xxlarge` and `padding_extra_large` are both 24dp under different names; `padding_tiny` (2dp) and `padding_micro` (3dp) are one unit apart. Candidates to collapse.
6. **Corner radius tokens aren't the source of truth yet.** Composables mostly hardcode a literal `.dp` value instead of referencing `dimens.xml`.
7. **No formal opacity scale.** A small named scale (e.g. 8/16/24/40/60/80%) would tighten this up.
8. **Legacy flat status colors.** `ColorGreen`/`ColorRed`/`ColorNeutral` duplicate `PulseStatusColors`'s light-mode text values without being theme-aware. Likely removable.
9. **`themes.xml` still ships default Material purple/teal.** Decide whether to re-skin it to match the Compose theme or leave it as pure launch scaffolding.

---

*Sourced from `ui/theme/Color.kt`, `Theme.kt`, `Type.kt`, `res/values{,-w600dp,-w936dp}/dimens.xml`, `res/values{,-night}/themes.xml`, and a grep sweep of `ui/` for shape, elevation, border, and alpha usage.*

*A visual, live-rendered version of this document (actual embedded fonts, live color swatches) is also available as a Claude Artifact.*
