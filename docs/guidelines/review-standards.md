# Review standards

What gets checked when reviewing a change in this repo. No committed lint/review-bot config
exists (no detekt/ktlint config, no CI workflow as of this writing) — these rules are enforced by
convention and by Claude Code's own `/code-review` skill, sourced directly from this project's
own strict rules rather than an external tool's config.

**Status:** restructured 2026-08-31 from root `CLAUDE.md`'s "Strict rules" section, generalized
so it survives regardless of which tool (human, `/code-review`, a future CI lint step) is doing
the checking.

1. **Initialize as `null`, not arbitrary defaults.** Only add a default where it's genuinely
   necessary, and flag it before doing so. See `@docs/guidelines/kotlin-style.md`.
2. **`@Preview` on every composable. No exceptions.** Use inline mock data. `*Route.kt` files are
   the one standing exception in practice, since they need a live Hilt `ViewModel` — previews
   belong on the stateless `*Screen.kt`/sub-composables they render. Wrap preview content in
   `MarketPulseTheme(theme = MarketPulseTheme.<PRESET>) { ... }`, never plain `MaterialTheme { ... }`
   — almost every component reads `LocalPulseColors.current` (directly or via
   `.textColor`/`.pillColor`), which throws ("PulseColors not provided") outside
   `MarketPulseTheme`. A preview that compiles but is wrapped in the wrong theme still fails to
   render in the IDE with no obvious compile error to point at. Current coverage gaps are tracked
   in `@docs/architecture/known-gaps.md` — don't extend them in new code.
3. **No hardcoded strings or dimensions in Compose files.** Always `stringResource()` /
   `dimensionResource()`.
4. **Comments explain the why, are self-contained, and never cite a spec, mockup, phase, or
   repo doc — in any file type, Kotlin or XML.** Preserve existing comments unless verifiably
   obsolete; update them when the code beneath changes. Full rules, examples, and a grep to run
   before finishing: `@docs/guidelines/comments.md`.
5. **Explain before you code.** For every non-trivial change, start with a short paragraph on the
   approach and the choices being made. That explanation goes in your reply, not in code
   comments.
6. **Localized changes only.** Do not refactor, reformat, or "clean up" outside the requested
   change. If you see something wrong, flag it — don't silently fix it (the exception: a known
   gap you're already touching for another reason — see `@docs/architecture/known-gaps.md`'s own
   "fix opportunistically" framing).
7. **Ask before assuming.** Ambiguous requirement, missing color token, unclear data model,
   unfamiliar acronym — stop and ask.
