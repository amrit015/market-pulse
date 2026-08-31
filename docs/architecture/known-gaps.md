# Known gaps

**⚠️ Living doc — verify against actual code before trusting.** This is a punch-list of confirmed
stale/inconsistent spots in the current codebase, not a description of the intended convention.
None of these are "the pattern to follow" — the opposite; each is a deviation from convention
that's tolerated until something else touches that file. Fix opportunistically when you're
already in the file for another reason; don't schedule cleanup work around this list without
sign-off.

**Status:** restructured 2026-08-31 from root `CLAUDE.md`'s "Known gaps" section and
`STYLE_SPEC.md §14`'s still-open decision items. Re-verified during the move except where marked
`(needs re-verification)`.

## Code-level gaps

- **`@Preview` coverage is incomplete.** Only `DashboardScreen.kt`, `MarketPostureView.kt`,
  `WeeklyPlaybookView.kt` have partial previews; `SummaryScreen.kt`, `IndicatorsScreen.kt`,
  `IndicatorDetailSheet.kt`, `IndicatorHorizonsScreen.kt`, and `ui/components/PulseCard.kt` have
  full coverage. The rest of the app is still the gap. The convention (`@docs/guidelines/compose-conventions.md`)
  stands for new code regardless.
- **Broken (not just missing) previews in `MarketPostureView.kt`, `WeeklyPlaybookView.kt`,
  `DashboardScreen.kt`, `NewsScreen.kt`, `UnifiedScoreHeaderCard.kt`.** All wrap preview content in
  plain `MaterialTheme { ... }` instead of `MarketPulseTheme { ... }` — since the composable
  inside reads `LocalPulseColors.current` (most do, directly or via `SignalColorExtensions`), the
  preview crashes at composition time with "PulseColors not provided" instead of rendering.
- **Hardcoded strings/dims in `DashboardScreen.kt`.** A literal `"Sector Rotation"` string and raw
  `dp` literals bypass the resource system.
- **`DatabaseModule` missing `@Singleton` on two DAO providers.** `provideMarketSummaryDao` and
  `provideMarketPostureDao` — every other DAO provider has it. Cosmetic if you're not touching
  that module.
- **`NewsModule` / `MarketRiskModule` parameter naming.** Both name their repository impl
  parameter `marketSummaryRepositoryImpl` — copy-paste leftover from `SummaryModule`. Cosmetic.
- **`DashboardApi.kt` is dead code.** Dashboard uses direct Firestore SDK, not REST. Do not "wire
  it up" — it's not a gap to fill, it's a leftover to eventually delete.

## Design-token audit items (from the 2026-08-16 Stock Analysis token pass)

Carried forward from `STYLE_SPEC.md §14`'s open items — not re-audited during this restructure,
so treat counts/specifics as `(needs re-verification)`:

- **8 of 12 typography styles (`Type.kt`) request a font weight that isn't bundled** — Montserrat
  Normal/SemiBold, Inter Bold/SemiBold. `(needs re-verification)`
- **Two spacing dimens likely collide:** `padding_tiny` (2dp) and `padding_micro` (3dp), one unit
  apart; `padding_xxlarge` and `padding_extra_large` both resolve to 24dp.
  `(needs re-verification)`
- **A handful of corner-radius literals bypass the `dimensionResource` token** (`6.dp`, `8.dp`,
  `12.dp` at a few call sites, as of the 2026-08-16 audit) even though `PulseCard`/`SignalPill`
  now centralize the majority of shape usage through the token system. `(needs re-verification)`
- **No formal opacity scale** — every `.copy(alpha = …)` call picks its own float independently.
  `(needs re-verification)`
- **`PulseTokens.Signal`'s `unknown` state (missing-data) has no resolved hex** —
  placeholder-mapped to the mode's own `onSurfaceMuted` since the original theme migration. Needs
  a real value from Design before a data-missing state looks intentional rather than "we forgot
  this." Still true as of 2026-08-31 (see `@docs/theming-system/theming-spec.md`).
- **`SignalPill`'s dark-mode text blend (40% toward white) was tuned live, mid-session**, well
  past an initial 12–15% estimate — worth a second look against real content on a device.
  `(needs re-verification)`
- **`themes.xml`/`values-night/themes.xml` still ship unused default Material purple/teal
  boilerplate** (`colorPrimary = purple_500`, etc.) below the `android:statusBarColor` fix that
  was already removed. Cosmetic, unused by Compose's own `MaterialTheme()`.
  `(needs re-verification)`

## Unconfirmed carry-overs

- **"Navy light hasn't had its own dedicated Design verification pass"** — noted in the
  2026-08-16 Stock Analysis branch log. Not independently checked during this restructure.
  `(needs verification)`
