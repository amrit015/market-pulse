# Market Pulse (Android) — Claude Code Memory

> Conventions and defaults, not hard rules — deviate when it makes sense, and say why. Detail
> lives in `/docs`, pulled in via the `@` references below (backtick-wrapped, so they're read
> on demand, not eagerly loaded every session — see `@docs/README.md` for why that matters).
>
> **Status:** restructured 2026-08-31 from a single flat `CLAUDE.md` + several root-level running
> logs into this index + a categorized `docs/` tree. Every claim below and in `docs/` was
> re-verified against source during that restructure, except where explicitly flagged otherwise.

## Project overview

Market Pulse is a Kotlin / Jetpack Compose (Material 3) Android client for an institutional-style
financial intelligence app — macro indicators, per-stock analysis, curated news, and AI-generated
market briefings. Package root is `com.marketlabs.pulse` (not `com.marketpulse.app`). Backend is
Firebase Cloud Functions v2 (not Cloud Run), reached via two different transport strategies
depending on the domain. Every feature is vertically sliced across the same five-layer package
shape, and data flows one direction only — see "Where things live" below.

## Commands

```bash
./gradlew installDebug   # Build and install debug on a connected device
./gradlew assembleDebug  # Just compile
./gradlew test           # Run unit tests
./gradlew lint           # Run lint
```

No detekt/ktlint config and no CI workflow exist in this repo as of this writing — `lint`/`test`
are run manually, not gated by anything automated yet.

## Where things live

- Module/package map, tech stack, the backend boundary → `@docs/architecture/overview.md`
- ViewModel/UiState shape, Route/Screen split, navigation graph → `@docs/architecture/android.md`
- Transport strategies, Room caching, `SyncManager`, a worked example → `@docs/architecture/data-flow.md`
- Backend field/flag-name contracts (the invisible cross-repo dependencies) → `@docs/architecture/cross-repo-contracts.md`
- Confirmed stale/inconsistent spots in the current code (verify before trusting) → `@docs/architecture/known-gaps.md`
- Hilt DI conventions, null handling, naming → `@docs/guidelines/kotlin-style.md`
- `PulseTabRow`, the glossary system, resource conventions → `@docs/guidelines/compose-conventions.md`
- What a review actually checks → `@docs/guidelines/review-standards.md`
- Color tokens, the card system, light/dark presets — **check this before adding a new color,
  card style, or badge** → `@docs/theming-system/theming-spec.md`. History/rationale for how it
  got this way → `@docs/theming-system/theming-history.md`.
- Card heading/spacing conventions (section-title vs. eyebrow header, content-heading sizing, the
  merged-card-with-dividers pattern) — piloted on Summary, LIVING doc, not yet applied to other
  screens → `@docs/theming-system/card-heading-conventions.md`.
- Product context for design work → `@docs/product-brief.md`
- Notion brain (product context, ADRs, cross-repo contracts, design system) —
  `https://app.notion.com/p/marketPulse-brain-3b07c8397e7b801abfc8f8ceb1d9fdae`. Ask before
  pulling large pages.

## Standing gotchas (code-verified)

- **Package root is `com.marketlabs.pulse`, not `com.marketpulse.app`.** Backend is Firebase Cloud
  Functions v2, not Cloud Run. Both are easy to get wrong by pattern-matching the project name.
- **Data flows one direction: Remote → Local (Room) → Repository → ViewModel → Screen.** A
  ViewModel never talks to a data source directly; a Screen never talks to a ViewModel's
  dependencies — only to the `StateFlow` it exposes.
- **Hilt DI is 100% `@Provides`-in-`object`, never `@Binds`.** Every existing module follows this;
  introducing `@Binds` would be the one inconsistent module in the codebase.
- **`@Preview` on every composable, wrapped in `MarketPulseTheme(theme = ...) { ... }`, never
  plain `MaterialTheme { ... }`.** Almost every component reads `LocalPulseColors.current`
  (directly or via `.textColor`/`.pillColor`), which throws ("PulseColors not provided") outside
  `MarketPulseTheme` — a preview that compiles still fails to render in the IDE with no obvious
  compile error to point at.
- **No hardcoded strings/dimensions in Compose files** (`stringResource()`/`dimensionResource()`
  always); **every card goes through `PulseCard`**, never a hand-rolled `Card(colors=…, border=…)`;
  **signal/theme colors go through `LocalPulseColors.current`**, never `MaterialTheme.colorScheme`,
  for anything the token system defines separately.
- **Comments are self-contained** — never cite a spec, ADR, or doc file by name. State the actual
  reasoning in the comment itself; a reader with only this repo checked out has to be able to
  follow it.
- **Ask before assuming; localized changes only.** Ambiguous requirement, missing token, unclear
  data model → stop and ask, don't guess. Don't refactor/reformat/"clean up" outside what was
  asked — flag what you notice instead of silently fixing it.
