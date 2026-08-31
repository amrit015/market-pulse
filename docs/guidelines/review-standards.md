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
4. **Comment thoroughly. Preserve existing comments** unless verifiably obsolete.
5. **Explain before you code.** For every non-trivial change, start with a short paragraph on the
   approach and the choices being made. Keep reasoning in prose, not comments.
6. **Localized changes only.** Do not refactor, reformat, or "clean up" outside the requested
   change. If you see something wrong, flag it — don't silently fix it (the exception: a known
   gap you're already touching for another reason — see `@docs/architecture/known-gaps.md`'s own
   "fix opportunistically" framing).
7. **Ask before assuming.** Ambiguous requirement, missing color token, unclear data model,
   unfamiliar acronym — stop and ask.
8. **Comments are self-contained — never cite a spec, ADR, or doc file by name or requested
   changes.** No `spec-YYYYMMDD-*.md`, no "Token Contract," no "Design Direction," no "per the
   migration table." A comment has to make sense to someone with only this repo checked out,
   nothing else open. Document the thought process and the actual implementation directly: what
   the code does, why it does it that way, what it replaced and why that mattered. If a rule
   genuinely originates from an external doc, restate the rule itself in the comment — don't
   point at the doc. (This applies to *code* comments specifically — the `docs/` tree itself is
   allowed to reference dates/history, since that's its job.)
