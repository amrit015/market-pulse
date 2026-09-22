# Code comments

How comments are written in this repo — every kind of source file: Kotlin (`//`, `/* */`, KDoc
`/** */`), Compose files, and XML (`<!-- -->` in `res/values/*.xml`, layouts, the manifest), plus
Gradle scripts. One rule set, no per-language exceptions.

**Status:** written 2026-09-21. Replaces the two comment rules that used to live in
`@docs/guidelines/review-standards.md` (items 4 and 8). Those rules only banned citing a doc "by
name", so unnamed references ("the spec", "the mockup", "Pass 4") kept slipping through; this
version bans the whole category.

## The one test

**A comment has to make sense to someone with only this repo's source open.** No spec, ticket,
mockup, chat thread, other repo, or `docs/` page open next to it. If understanding the comment
requires opening something else, the comment is incomplete — restate the substance in it.

## What a comment is for

Comment the *why*, not the *what*. The code already says what it does; a comment earns its place
when a reader could not recover the reason from the code alone:

- A constraint or platform quirk that forced an unusual shape (`minSdk 26 has no RenderEffect, so
  no blur`).
- A deliberate omission or non-default choice a reader would otherwise "fix" (`not PulseCard, on
  purpose: this is a gallery of the card styles themselves`).
- A backend contract the client depends on (state the rule and the field names — see below).
- The failure a non-obvious choice avoids, when someone could otherwise "simplify" it back into a
  bug (stated as a present-tense constraint — see "Describe the current implementation" below).
- A non-obvious invariant, ordering requirement, or threading assumption.

"Comment thoroughly" means the *reasoning is complete*, not that there is a lot of it. Don't
narrate obvious code, don't restate a name, and don't leave a one-word comment where the reason is
subtle. Match the density of the surrounding file.

## Never reference a planning artifact — by any name

Comments describe the code as it is, not the process that produced it. Do not cite, name, number,
or allude to any of these, in any wording:

| Banned | Examples of the wording to avoid |
|---|---|
| A spec, brief, PRD, ticket, or ADR | `spec-YYYYMMDD-*.md`, "per the spec", "the spec's table", "spec §6", "the design brief" |
| A mockup or design file | "the mockup", "the Design mockup's layout", "per the Figma", "a pixel-match of the mockup" |
| A named contract or direction document | "Token Contract", "Design Direction", "the migration table", "this app's token contract" |
| A repo doc, by path or name | `docs/…/*.md`, `card-heading-conventions.md`, `theming-spec.md`, `collapsing-header-tabs.md`, `CLAUDE.md`, "see `overview.md`" |
| A place inside a plan | `§2`, "Pass 4", "Phase 2", "Layer 3", "the Tenth step", "Part B1", "P2" |
| Revision history of a plan | "(revised)", "(revised again)", "follow-up", "converged on…", "net-new for this spec" |
| Another repo's file or commit | `api/marketPulse.ts`, "backend change `54c2f93`" |

Why each is off limits: the artifact may be renamed, deleted, or never have been checked in; a
section number rots the moment the plan is edited; and "revised" says a plan changed without
saying what the code now does.

Backend **field, flag, and endpoint names are fine** — they are the contract itself, not a
pointer to a document (`last_updated`, `chips_added`, `resolveActiveSymbols()`). Name them, and
state the rule that applies to them.

### What to write instead: keep the reasoning, drop the pointer

The banned comment almost always contains a real reason that the citation was standing in for.
Extract that reason and write it out.

```kotlin
// Bad — the reader needs the spec to know what "same reasoning" means
// spec-20260915-compliance-disclaimers.md §2: same reasoning as initialTheme above.

// Good — self-contained
// Read the accepted terms version synchronously, like initialTheme above: the first frame has to
// already know whether to show the onboarding flow, or a returning user sees it flash by.
```

```kotlin
// Bad
/** Sized per `docs/theming-system/card-heading-conventions.md`'s Tenth step. */

// Good
/** 15sp/SemiBold — the size every card's content heading uses, so a stat label never out-ranks
 *  its card's own title. */
```

```xml
<!-- Bad -->
<!-- Per the design mockup; see CLAUDE.md. -->

<!-- Good -->
<!-- Elevation is 0 everywhere: the card system separates surfaces with a hairline border and a
     fill, never a drop shadow. -->
```

If you genuinely do not know the reason (the artifact is gone and the code doesn't reveal it),
**say less rather than invent one** — delete the citation and keep only what the code verifiably
does, or ask.

An external, publicly resolvable fact is fine to name — a platform API level, a library's
documented behaviour, a public standard (`WCAG AA 4.5:1`) — because a reader can look it up
without this repo's tooling. Still prefer stating the consequence over just the name.

## Describe the current implementation, not its history

A comment describes the code **as it is now**. It is not a changelog, and git history already is
one. A reader opening the file next year has no use for what the code looked like before.

Don't write:

- **Dates or versions of a change** — "2026-08-29: darkened…", "bumped from 15% to 18%",
  "(new 2026-08-21)", "added in v2".
- **Before/after narration** — "was `List<String>`, now a structured type", "used to be a
  standalone label", "previously reserved-but-empty space", "no longer clickable", "moved off X onto
  Y", "replaced the old…", "removed the nested card", "fixed the gap".
- **Who asked or why the change happened** — "was asked to keep its look", "after a complaint",
  "tried a slide first and dropped it".
- **Provenance labels** — "(Claude-generated content)", "revamp", "rewrite".

Write the current rule and the reason for it instead:

```kotlin
// Bad — a log of how the value got here
// 2026-09-15: bumped from 15% to 18% -- cards were reading too faint against the page.

// Good — the value and why it is what it is
// 18% accent blend (vs. the page background's 5%): lower and a synthesis card is hard to tell
// apart from the page and from the DATA-style cards next to it.
```

```kotlin
// Bad
// Was `currentPage`; changed to `settledPage` after a mid-swipe bug.

// Good
// `settledPage`, not `currentPage`: `currentPage` reports an intermediate, still-in-flight value
// mid-swipe, which would be pushed to the ViewModel and fight the gesture still running.
```

**Essential history is the only exception** — history the code cannot make sense without. The
usual cases: a database migration (the old on-disk shape is *why* the migration exists, and what it
must do with existing rows), and a compatibility shim for data that is still out there in the old
shape. Keep it undated, factual, and short. If you can express the same thing as a constraint on
today's code, do that instead.

**Dates are allowed only for a time-sensitive external fact the code relies on**, stated with what
was checked: "CME's published contract list, checked 2026-08-24". Never as a change stamp.

A `TODO` says what behaviour is missing and why it isn't there yet. A bare `// TODO` is noise.

## Keeping comments true

- **Preserve existing comments that explain current behaviour** unless they are verifiably
  obsolete — they usually record a bug that was hit and fixed. If one is written as history, keep
  its reasoning and restate it as a present-tense rule rather than deleting it.
- **When you change code, update the comment above it in the same edit.** A comment describing
  behaviour that no longer exists is worse than none. If a comment is obsolete and you can prove
  it, delete it rather than leaving it stale.
- Don't reword comments you weren't asked to touch. The exception: a comment on a line you are
  already editing that breaks the rules above gets fixed in the same change.

## Compose and XML specifics

- **KDoc on a composable** says what it renders and the non-obvious contract for callers (which
  slot is optional, why a parameter is nullable, what state it hoists). It doesn't restate the
  parameter list.
- **Preview functions** need no comment beyond what their name says.
- **`dimens.xml` / `colors.xml` / `strings.xml` group headers** describe the *role* of the group
  ("padding inside a card, not between cards"), not where the values came from.
- **A comment on a string** says where it appears and any constraint on it (`shown under the
  gauge; must fit on one line`), not which document the copy came from.

## Before you finish a change

Grep what you touched for planning vocabulary and fix any hit that isn't a real backend name:

```bash
git diff -U0 | grep -E '^\+' | grep -nE "spec-[0-9]{8}|\bspec\b|\.md\b|mockup|design brief|[Tt]oken [Cc]ontract|§|\b(Pass|Phase|Layer) [0-9]|(revised)|20[0-9]{2}-[0-9]{2}"
```

Whole-tree audit (comment lines only):

```bash
grep -rnE "^\s*(//|\*|/\*|<!--).*(spec-[0-9]{8}|\bspec\b|\.md\b|mockup|design brief|[Tt]oken [Cc]ontract|§|\b(Pass|Phase|Layer) [0-9]|\(revised|20[0-9]{2}-[0-9]{2})" app/src --include=*.kt --include=*.xml --include=*.kts
```

Judge each hit — "Layer 2" in a comment about an actual architectural layer is fine, and an ISO
date used as a format example (`"2026-08-01" -> "Aug 1"`) is not a change stamp — but don't
blanket-suppress. Also skim for undated history: `used to`, `previously`, `no longer`, `the old`,
`moved off`, `is now`, `revamp`, `Generated by`.
