# docs/

Detail behind root `CLAUDE.md`'s "Where things live" section. `CLAUDE.md` is the always-loaded
index; this folder is the reference material it points into.

## Structure

```
docs/
  README.md                        — this file
  product-brief.md                 — product context for design work (single topic, no folder)

  architecture/                    — descriptive: how the system is actually built, today
    overview.md                      module/package map, tech stack, the backend boundary
    android.md                       Compose/nav-specific structure: ViewModel/UiState shape,
                                      Route/Screen split, navigation graph
    data-flow.md                     transport strategies, Room caching, SyncManager, one
                                      traced example (the `stocks` domain)
    cross-repo-contracts.md          invisible Android↔backend field/flag-name dependencies
    known-gaps.md                    LIVING doc — confirmed stale/inconsistent spots in the
                                      current code; verify before trusting

  guidelines/                      — prescriptive: how to write new code in this repo
    kotlin-style.md                  DI (Hilt @Provides-in-object), null handling, naming
    compose-conventions.md           PulseTabRow, glossary system, resource conventions
    review-standards.md              what a review (human or `/code-review`) actually checks

  theming-system/                  — a cohesive domain with both a spec and a history that
                                      constantly cross-reference each other
    theming-spec.md                  the "what do I use" doc — color tokens, card system,
                                      pills, typography, spacing. Most tasks need only this one.
    theming-history.md               the "why" — how the theming/card system got here,
                                      chronological, read rarely
```

## How this connects to `CLAUDE.md`, and why it doesn't cost tokens by default

`CLAUDE.md` supports `@path/to/file` import syntax. By default this is eager: the moment
`CLAUDE.md` loads, Claude Code recursively inlines the full content of every `@`-referenced file
into context — up to 5 hops deep — whether or not the current task needs it. Do this for a dozen
reference docs and every session pays for all of them, always.

The exception that makes this pattern work: **imports are not evaluated inside a markdown code
span or code block.** Wrap the same reference in backticks —

```
Module map → `@docs/architecture/overview.md`
```

— and it renders as inert text: a pointer for the agent to read, not a directive to eagerly
inline. The agent decides, per task, whether the topic matches, and only then calls `Read` on the
actual file.

This gives two deliberate tiers of memory:

| Tier | Where | Cost | Use for |
|---|---|---|---|
| Eager | `CLAUDE.md`'s own body (prose, not a backtick-wrapped `@` reference) | Paid every session, unconditionally | A small number of high-frequency, code-verified facts that apply to almost any task in this repo |
| Lazy | `docs/*.md`, referenced via backtick-wrapped `@` pointers | Paid only when a task's topic matches | Everything else — architecture detail, conventions, audits, reference tables |

Removing the backticks from a pointer is a real, consequential decision — it turns that file into
a standing cost on every future session. Only do that for something that truly belongs in every
session's starting context; usually the better move is to shorten it and fold it into
`CLAUDE.md`'s own "Standing gotchas" section instead.

**How to verify this is actually working:** look at what got injected into a fresh session's
context at start. If `CLAUDE.md` loaded but the referenced `docs/*.md` files' content did not
also appear, the backtick-wrapping is doing its job.

## Conventions for maintaining these docs

- **New doc → new backtick-wrapped `@docs/...` pointer in `CLAUDE.md`'s "Where things live"**, or
  it's undiscoverable.
- **Keep each doc scoped to one topic** — a task should need to read one file here, occasionally
  two, not several.
- **Verify before writing it down, and date the verification.** A one-time structural scan
  surfaces shape, not truth — note the date when you confirm or correct a claim so the next
  reader can judge freshness.
- **Living/status docs get an explicit "verify before trusting" flag at the top.**
  `architecture/known-gaps.md` is this repo's one living doc so far — anything describing
  in-flight work should say so plainly, not imply it's settled.
- **Check `git merge-base --is-ancestor <commit> HEAD`** before citing branch-local work as
  current state — a scan of a checked-out worktree can't tell merged from unmerged history on its
  own. (This repo's docs were restructured 2026-08-31 from several branch-local running logs that
  had drifted into describing unmerged/in-progress state as current fact — the kind of drift this
  check catches.)
- **Treat backtick-removal on an `@` pointer as a real decision, not a typo fix.** It flips a file
  from lazy to eager for every future session.
