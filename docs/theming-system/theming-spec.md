# Theming spec — what to use

The "what do I use" reference for colors, card styles, pills, tabs, typography, and spacing.
Check this before adding a new color, card style, or badge — this system almost certainly already
covers it. The "why it got this way" narrative (migration history, what was tried and rejected)
is in `@docs/theming-system/theming-history.md`; read that only when you need the backstory, not
to find out what to call today.

**Status:** verified directly against source 2026-08-31 (`ui/theme/Color.kt`,
`MarketPulseTheme.kt`, `PulseColors.kt`, `ui/components/PulseCard.kt`,
`ui/components/widgets/SignalPill.kt`, `res/values/dimens.xml`). Sections marked
`(needs re-verification)` are carried over from an earlier audit and weren't re-checked.

## 1. Theme architecture — 10 presets, three token layers

The app supports **10 baked-mode presets** — 5 light (Plum, Navy, Fuchsia, Graphite, Teal), 5
dark (Lilac, Sky, Sand, Rose, Aqua). Picking a preset **is** the appearance setting — there's no
separate "follow system" toggle, and `isSystemInDarkTheme()` is never consulted for color
decisions. Selection persists via DataStore (`ThemeRepository`), default is `LILAC`.

Every preset resolves from the same three data sources in `PulseTokens` (`ui/theme/Color.kt`):

| Layer | Varies by preset? | What it covers |
|---|---|---|
| **Signal** | No — locked per mode | Bullish/bearish/neutral/warning meaning. Every light preset paints the same 8 hexes; every dark preset paints the same (different) 8 hexes. `Accent` is never allowed to override these. |
| **Surface** | No — locked per mode | The shared neutral ramp (background base, surface, elevated surface, outline, on-surface text). One instance for all 5 light presets, one for all 5 dark. |
| **Accent** | **Yes — the one thing that changes** | 5 values per preset: `primary`, `on`, `surface` + `surfaceBorder`, `tinted`. |

Resolution happens once per composition, in `MarketPulseTheme.toPulseColors()` /
`.toColorScheme()` (`ui/theme/MarketPulseTheme.kt`) — every screen reads either
`MaterialTheme.colorScheme` (standard M3 roles) or `LocalPulseColors.current` (the extended
tokens below), never `PulseTokens` directly.

## 2. Signal tokens (locked)

`PulseTokens.Signal` in `Color.kt`.

| Role | Light text | Light pill | Dark text | Dark pill |
|---|---|---|---|---|
| Bullish | `#1B5E20` | `#C6E4C1` | `#81C784` | `#1F4A28` |
| Bearish | `#B71C1C` | `#F8C8BE` | `#F37B7B` | `#4A2222` |
| Neutral | `#B36A00` | `#F0DEB0` | `#FFD54F` | `#4A3820` |
| Warning | `#BF360C` | `#F5CFB8` | `#E65100` | `#4A2210` |

`signal.unknown` (the fourth state, for missing data) has **no resolved hex** — placeholder-mapped
to the mode's own `onSurfaceMuted`. Still true as of this verification; see
`@docs/architecture/known-gaps.md`.

Access pattern: never branch on the `SignalColor` enum by hand. Use `SignalColor.textColor` /
`.pillColor` (`ui/theme/SignalColorExtensions.kt`).

## 3. Surface ramp

`PulseTokens.Surface` in `Color.kt` — one instance for all 5 light presets, one for all 5 dark.

| Role | Light | Dark |
|---|---|---|
| `background` | **computed, not a flat hex — see §5** | `#0D0E12` |
| `surface` | `#FBFAF7` | `#17181D` |
| `surfaceElevated` | `#FFFFFF` | `#1F2026` |
| `onBackground` / `onSurface` | `#14161B` | `#F0EEF3` |
| `onSurfaceMuted` | `#585A61` | `#AFB0B6` |
| `outline` | `#E4E2DC` | `#2A2B31` |

`Color.kt`'s own `background` field for light mode holds only a neutral `#FFFFFF` base —
`toColorScheme()` blends in a per-preset accent tint on top (§5). Dark mode's `background` is a
plain flat token, untouched.

## 4. Accent tokens — all 10 presets

`PulseTokens.Accent` in `Color.kt`. `tinted` is the *literal* per-preset value — §5 covers what
price cards actually render (a runtime blend, not this raw hex).

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

## 5. Derived tokens — computed, not stored as constants

Several tokens are computed at runtime, once per preset resolution, rather than hand-picked hex —
so a future "make this a bit more/less pronounced" ask is a one-line factor edit, not
re-deriving 10 hex values.

| Token | Formula | Used by |
|---|---|---|
| Light-mode page `background` | `lerp(white base, accent.primary, 0.05)` | `colorScheme.background`/`surfaceContainerLowest`/`surfaceDim`, computed once in `toColorScheme()` and reused for all three so they can't drift apart. Dark mode: untouched flat token. |
| `surfaceTinted` | `lerp(accent.tinted, accent.surface, 0.45)` | `PulseCard(style = DATA_SPARKLINE)` only (see §6) |
| `accentSurfaceStrong` — **dark mode** | `lerp(lerp(accent.tinted, accent.surfaceBorder, 0.55), Color.Black, 0.12)` | `PulseCard(style = SYNTHESIS)` background, dark mode |
| `accentSurfaceStrong` — **light mode** | `lerp(Color.White, accent.primary, 0.15)` | `PulseCard(style = SYNTHESIS)` background, light mode |

`accentSurfaceStrong` is deliberately computed differently per mode, not one shared formula: the
dark-mode formula (darken an existing blend toward black) doesn't reliably darken in light mode
too — `accent.surfaceBorder` isn't consistently the darker anchor across all 10 presets, so light
mode instead blends white directly toward `accent.primary`, a hue-tuned light tint rather than a
darkened one. Both approaches are tuned by eye against the finished app, not derived from one
shared contrast target.

`colorScheme.primary`/`onPrimary` mirror `accentPrimary`/`accentOn` exactly (not a second source
of truth) — purely so built-in M3 components (ripples, default `Switch` tinting) that only read
`colorScheme.*` stay coherent with the accent. App code should always read
`LocalPulseColors.current`, never `MaterialTheme.colorScheme`, for anything this token system
defines separately.

`error`/`errorContainer`/`onError`/`onErrorContainer` have **no resolved hex in this system** —
left out of the `ColorScheme` builder calls so M3's own baseline default applies, same reasoning
as `signal.unknown`.

## 6. Card system — `PulseCard`

Every content card goes through one shared composable, `PulseCard(style: PulseCardStyle)`
(`ui/components/PulseCard.kt`), never a hand-rolled `Card(colors=…, border=…, shape=…)`. Three
styles:

| Style | Background | Border | Shadow | Corner radius | Used by |
|---|---|---|---|---|---|
| `DATA` | `colorScheme.surfaceVariant` (= `surfaceElevated`: white in light, `#1F2026`-family grey in dark) | None, either mode | Yes | `corner_radius_card_large` (16dp) | Raw/computed readings — VIX, Fear & Greed, Put/Call, most of Indicators/Positioning/Posture, Overview's non-sparkline asset cards |
| `SYNTHESIS` | `accentSurfaceStrong` (§5, per-mode formula) | `accentSurfaceBorder`, 1dp | Yes | `corner_radius_card` (12dp) | AI-authored/curated content — briefings, verdicts, news |
| `DATA_SPARKLINE` | `surfaceTinted` (§5) | `accentSurfaceBorder`, 1dp | Yes | `corner_radius_card_large` (16dp) | Overview's per-asset cards that embed a live intraday `SparklineChart` only — frozen to `DATA`'s pre-2026-08-29 look so the sparkline keeps reading against the same background it always has |

A card's *shadow* doesn't vary by style — all three get the same treatment (§ below). Only
background/border vary by style now; that wasn't always true (see history doc for the
"NEUTRAL" style that came and went, and the multiple `SYNTHESIS`-scope narrowings).

**Deliberately excluded** from this system (still plain `Card`, on purpose): `HorizonNavigationCard`
(a CTA button, not a content card), `UnifiedScoreHeaderCard`/`UniversalGaugeCard` (background is
signal-owned, passed in by the caller), `PresetSwatchCard` (must render a *different* preset's raw
colors regardless of the active theme), the sector rotation heatmap tiles.

### Shadow mechanism

Applied by hand via `Modifier.shadow(elevation, shape, ambientColor, spotColor)`, not
`CardDefaults.cardElevation` — Material's default elevation shadow only exposes one number and
renders as a directional key-light cast (heavy at the bottom, faint everywhere else).
`ambientColor` (a soft, roughly even halo) is weighted well above `spotColor` (the directional
cast) to trade that for something closer to an even halo on every side.

| | Light mode | Dark mode |
|---|---|---|
| Elevation | `elevation_medium` = 13dp | `elevation_small` = 3dp |
| `ambientColor` alpha | 0.50 | 0.24 |
| `spotColor` alpha | 0.24 | 0.10 |

Light mode's numbers are deliberately stronger than dark's: light-mode cards sit on a
near-white/tinted-white page where the shadow is the main thing separating a card from it, while
dark mode already gets most of that separation from the color step between the near-black
background and the lighter-grey card. Mode is detected via
`colorScheme.background.luminance() < 0.5f` inside `PulseCard` itself — there's no formal `isDark`
`CompositionLocal` threaded down from the top-level `MarketPulseTheme(theme, content)` call.

**Gotcha: never put `animateContentSize()` on a `PulseCard`'s own outer `modifier`.** It clips
whatever it wraps to its own animated rectangle every frame; sitting outside `PulseCard`'s shadow
modifier in the chain, that rectangular clip cuts straight through the shadow's rounded corners —
a flat grey sliver past the bottom edge instead of a clean rounded shadow. Put it on the inner
content `Column` instead, so only that Column's own content is clipped as it grows/shrinks.
Every expandable `SYNTHESIS` card in the app (`SynthesisHeroCard.kt`, Dashboard's Technical
Briefing, Indicators' AI Executive Briefing, `DirectNews.kt`'s news cards) follows this.

## 7. Signal pill system — `SignalPill`

Every small colored badge — sentiment tags, impact levels, status pills, regime pills, directional
change indicators — goes through `SignalPill` (`ui/components/widgets/SignalPill.kt`).

- Background: whichever `signal.*.pill` (or accent-border-adjacent) color the caller passes in.
- Padding: `padding_medium` (8dp) horizontal / `padding_small` (4dp) vertical,
  `corner_radius_pill` (fully stadium-shaped).
- **Text color is blended from the signal token, direction depends on mode** — computed inside
  `SignalPill`, not baked into `signal.*.text` itself, since those tokens are also used as plain
  standalone text elsewhere at full strength:
  - Light mode: blend toward black, 20%.
  - Dark mode: blend toward white, 40% `(needs re-verification — tuned live, may warrant a second look)`.
  - Mode detected via `colorScheme.background.luminance() > 0.5f`.
- `outlined: Boolean` variant — transparent fill + a `contentColor`-toned `border_medium` stroke
  (not `pillColor`-toned, which reads washed-out at any stroke width) — for supporting-context
  states that shouldn't compete with a card's own primary filled pill (e.g. Indicators'
  alignment/agreement/shift-direction pills).

`DirectionalChangePill` wraps it for price/ratio changes: a filled triangle (up/down), text is
unsigned magnitude only (the triangle carries the sign), and a `FLAT` state (flat-bar icon,
neutral tone) for an exact 0.0% reading.

## 8. Typography — card title convention

`AppTypography` in `Type.kt` has the full type scale `(needs re-verification for the
bundled-weight table — see @docs/architecture/known-gaps.md)`. The one theming-relevant
convention: card titles use one of two tiers, by card style (§6):

- **`DATA`/`DATA_SPARKLINE`-style card titles** — `titleMedium.copy(fontWeight = Bold)`, 17sp
  bold.
- **`SYNTHESIS`-style card titles** — `titleSmall`, 15sp semi-bold, no weight override.

Card body/subtitle/explainer text is always `onSurface`, matching its title — not
`onSurfaceVariant`/`colorScheme.secondary`. The two carve-outs: genuine timestamp strings
("Analyzed as of…") and text with an intentional signal/condition color.

## 9. Spacing, corner radius, borders

| Padding token | Value |
|---|---|
| `padding_tiny` | 2dp |
| `padding_small` | 4dp |
| `padding_medium` | 8dp |
| `padding_standard` | 12dp |
| `padding_large` | 16dp |
| `padding_xlarge` | 20dp |
| `padding_xxlarge` | 24dp |

| Corner radius token | Value |
|---|---|
| `corner_radius_chip` | 6dp |
| `corner_radius_small` | 8dp — `PulseTabRow`'s shape (see `@docs/guidelines/compose-conventions.md`) |
| `corner_radius_card` | 12dp — `PulseCard`'s `SYNTHESIS` default |
| `corner_radius_card_large` | 16dp — `PulseCard`'s `DATA`/`DATA_SPARKLINE` default |
| `corner_radius_pill` | 50dp — `SignalPill`'s and `FloatingBottomNav`'s shape |

| Border/elevation token | Value |
|---|---|
| `border_thin` | 1dp — `SYNTHESIS`/`DATA_SPARKLINE` card border, `SignalPill`-adjacent borders |
| `border_medium` | 3dp — `SignalPill`'s `outlined` variant stroke |
| `border_thick` | 6dp |
| `elevation_small` | 3dp — `PulseCard` shadow elevation, dark mode |
| `elevation_medium` | 13dp — `PulseCard` shadow elevation, light mode |
| `nav_elevation` | 2dp — `FloatingBottomNav`'s own native `tonalElevation`, a separate mechanism from `PulseCard`'s hand-drawn shadow (no blur — minSdk 26 has no `RenderEffect` to build one from, so it's a solid `shadowElevation`) |

Cards routinely cast a shadow now — the "flat/no-shadow by convention" framing from before the
2026-08-29/31 redesign no longer applies (see history doc).
