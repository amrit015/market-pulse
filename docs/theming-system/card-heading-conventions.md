# Card heading conventions — piloted on Summary, rolling out screen by screen

> **Status: LIVING / IN PROGRESS.** This documents a set of card-heading/spacing conventions
> established on the Summary screen (`ui/screens/summary/views/SummaryScreen.kt`) during an
> initial 2026-09-05 pass, with the explicit intent to roll the same conventions out to other
> screens once they're settled. As of 2026-09-05 that rollout is underway (see "Resolved" below for
> exactly what's landed on Indicators/Insights/Stock Detail/Deep Dive so far, including six Stock
> Detail sections moved from a page-level heading onto one `PulseCard` each) but still partial --
> most of News, and several DATA-style cards elsewhere, are untouched. Several open questions are
> called out explicitly at the bottom rather than silently resolved — read those before extending
> this pattern to a new screen. Verified against source on 2026-09-05; re-check line numbers before citing them
> if this file is read much later.

This is a companion to `theming-spec.md` §6 (card system) and §8 (typography — card title
convention), not a replacement. §8's existing DATA/SYNTHESIS two-tier table describes each card's
*outer* title tier; it predates the two more specific header families below and doesn't yet
mention either by name.

## Two header families

Every card on Summary now uses one of two distinct header treatments, chosen by what kind of card
it is — not interchangeably.

### 1. Section-title header — list/data cards

A small-caps, bold title sitting at the very top of the card, followed by a full-bleed divider
before any content.

```kotlin
Text(
    text = stringResource(id = R.string.section_x).uppercase(),
    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
)
HorizontalDivider(
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    thickness = dimensionResource(id = R.dimen.border_thin)
)
```

**Used by:** `DriversSection` (SummaryScreen.kt:505), `MarketPositionSection` (:573),
`WhatsNewSection` (:715), `WatchSection` (:796), `RisksSection` (:849), `LeadStoriesSection`
(:991), `MacroMixSection` (:1134), `DominoCard` (:1201). All `PulseCardStyle.DATA`.

The divider is full-card-width — it sits *outside* any padded `Column`/`Row` (at the outer,
unpadded `Column` level), not inset to the content margin. This is the same "divider spans the
full width between two separately-padded blocks" idiom `theming-spec.md` doesn't yet name but
several cards on this screen already use.

### 2. Eyebrow header — AI-narrative / headline-led cards

The shared `CardEyebrowLabel` composable (`ui/components/widgets/CardEyebrowLabel.kt`) —
`labelSmall`, bold, auto-uppercased, no manual `.uppercase()` call needed at the call site.

```kotlin
CardEyebrowLabel(
    text = stringResource(id = R.string.section_x),
    color = MaterialTheme.colorScheme.primary
)
Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
// content directly below
```

**Used by:** `SignalSection` ("Market Signal", :301), `MarketSentimentCard` ("Market Sentiment",
:1059), `TheReadSection` ("Market Read", :921, and "Where Capital's Moving", :952),
`MarketPositionSection`'s "What Changed" sub-block (:680). All `PulseCardStyle.SYNTHESIS`, or a
SYNTHESIS-flavored moment inside an otherwise-DATA card (What Changed).

**The gap between the eyebrow and whatever follows it is `padding_medium` (8dp), everywhere.**
This was inconsistent as of 2026-09-04 (Market Read had a 0dp gap, Where Capital's Moving had
`padding_tiny`) and was fixed to match Market Sentiment's `Spacer(paddingMedium)` on 2026-09-05 —
don't reintroduce a smaller gap on a new eyebrow-header card.

**Which family should a new card use?** Section-title for a card whose job is to *list* discrete
items (even a single-item "list" like Domino Effect's 3 timeline steps) — the divider under the
title visually separates "here's what this card is" from "here's the data." Eyebrow for a card
whose job is to *say one AI-authored thing* — headline first, eyebrow is a small tag identifying
the voice/topic, not a list-boundary marker. This rule is inferred from the current 12 cards, not
written down anywhere before this doc — flag it for review before treating it as settled.

## Content headings — the actual headline/label text, not the card-level header

Distinct from both header families above: the specific piece of text that says what an individual
entry *is* (a story's headline, a risk's description, Market Sentiment's own headline, Market
Signal's `signalLine` flash). As of 2026-09-05, size is unified across the whole screen:

| Card | Field | Style |
|---|---|---|
| Market Signal | `signalLine` | `titleMedium.copy(fontWeight = Bold)` |
| Market Sentiment | `headline` | `titleMedium.copy(fontWeight = Bold)` |
| Lead Stories | each story's `headline` | `titleMedium` (no weight override) |
| Macro Mix | each item's `headline` | `titleMedium` (no weight override) |
| What to Watch | each item's `label` | `titleMedium` (no weight override) |
| Risks to Market | each item's `risk` | `titleMedium` (no weight override) |

All `onSurface`. **Size is now consistent (`titleMedium` everywhere); weight is not** — Signal and
Sentiment are bold, the four list-style cards are plain. This split wasn't a deliberate decision,
just what existed before this pass touched only size; call it out explicitly if a future pass
unifies weight too; don't assume it's already settled just because size is.

## The "merged card" pattern — one card, many entries, dividers between them

Established this pass for `LeadStoriesSection`, `MacroMixSection`, `WatchSection`,
`RisksSection` — previously each of these rendered N separate `PulseCard`s (one per item); now
each renders exactly one card holding every item.

```kotlin
PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
    Column {
        // section-title header (see above), then its divider
        items.forEachIndexed { index, item ->
            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                Text(item.heading, style = titleMedium, color = onSurface)
                item.tag?.let {
                    Spacer(height = padding_medium)
                    TagPill(...) // or SignalPill for Risks' severity
                }
                item.body?.let {
                    Spacer(height = padding_medium)
                    Text(it, style = bodyMedium, color = onSurface)
                }
            }
            if (index != items.lastIndex) {
                HorizontalDivider(...) // full width, at the outer Column level
            }
        }
    }
}
```

Two rules worth preserving when extending this pattern:

- **The tag/pill/badge never shares a row with the heading.** It sits on its own line directly
  below, gated behind a `Spacer(padding_medium)` — see resolved item below; this was `padding_small`
  until 2026-09-05, when it was unified with the eyebrow family's gap. This was a direct fix
  (2026-09-05) for tags crowding long headings when both were forced onto one `Row`.
- **A divider only ever marks the boundary *between two different entries*, never within one
  entry's own heading/tag/body.** The older per-item cards (`LeadStoryCard`, `MacroCard`) each had
  an internal divider separating their own headline from their own summary — that's gone. If a
  single entry has multiple pieces of text, they're just stacked with `Spacer`s, no rule.

`MacroMixSection` additionally dropped its accent-tinted left-rail bar (a colored
`Box(fillMaxHeight, width = border_medium)` down the left edge of each entry) on 2026-09-05 — the
entries are now plain padded blocks like every other merged-card section. If a left-rail treatment
is wanted again elsewhere, treat it as a new decision, not a reversion of a mistake.

## Drivers — a card with two independent tap targets

`DriversSection` (SummaryScreen.kt:489) is the one card on this screen where the header and the
content below it lead to *different* actions, so it can't just be one whole-card `onClick`:

- The header row (section title + info icon) navigates nowhere on its own; only the info icon
  (its own nested `Modifier.clickable`) does anything, opening `DriversInfoBottomSheet`.
- Only the content row *below* the divider (the driver pills + trailing chevron) is clickable,
  via `Modifier.clickable(onClick = onClick)` on that `Row` directly — **not** `PulseCard`'s own
  `onClick` parameter, which would make the whole card (including the header) a single tap target
  and swallow the info icon's own nested click.

If a future card needs the same "info affordance in the header, navigate affordance in the body"
split, this is the reference implementation, not `PulseCard(onClick = ...)` wrapping everything.

## Resolved (2026-09-05, after the rest of this doc was first written)

- **Heading → tag/body gap is now `padding_medium` (8dp) everywhere**, unified with the eyebrow
  family's gap. It was `padding_small` (4dp) in the merged-card pattern (Lead Stories/Macro
  Mix/Watch/Risks) when this doc was first written, checked side-by-side against Market
  Sentiment's `padding_medium` gap on request, and changed to match rather than the other
  direction — `padding_medium` was already the fixed rule for four other cards (Market
  Sentiment/Market Read/Where Capital's Moving/What Changed), so the smaller value was the
  outlier. There is now exactly one "label/heading to what follows it" gap value across the whole
  screen, not two.
- **First step of the rollout to other screens (open item #4 below): Indicators' "Today's Read"
  (`AiExecutiveBriefingHero`, `IndicatorsScreen.kt`) and Insights' "Digest"
  (`SynthesisHeroCard.kt`, shared by Posture/Positioning/Risks/Playbook) were checked against
  Market Signal.** Three issues found and fixed, 2026-09-05:
  1. Both used `padding_large` (16dp) between the headline and the supporting body text below it
     (`alignmentNote` / `detail`), where Market Signal's own convention (and every eyebrow-family
     card on Summary) uses `padding_medium`. Changed both to `padding_medium`.
  2. Both put the expand/collapse chevron in the *same row* as the eyebrow label, with no explicit
     `Modifier.size(...)` on that icon — so the row's height was driven by the icon's unsized
     default (~24dp) rather than the eyebrow text's own height (~11-14dp). That extra slack made
     the perceived top padding and the eyebrow-to-content gap read as bigger than Market Signal's,
     even though every `Spacer` value was numerically identical. Market Signal has no icon on its
     eyebrow row at all, and Market Sentiment (which does need an expand/nav affordance) puts its
     chevron on the *headline* row instead, explicitly sized to `padding_large` — not on the
     eyebrow row. Fixed both cards to match that pattern: eyebrow row is now the bare label (icon
     stays only inside `CardEyebrowLabel` itself, sized to font per that composable's own logic),
     and the expand/collapse chevron moved to sit beside the headline text, sized to
     `padding_large`.
  3. (Consequence of #2, not a separate fix) The eyebrow→pill/headline gap now measures correctly
     as `padding_medium` with no added row-height slack, matching Market Signal exactly.

  This is a narrow, targeted fix to these two specific cards, not a claim that Indicators/Insights
  have otherwise been brought in line with this doc — see open item #4, still unresolved for the
  rest of each screen. Worth generalizing into a rule above once a second future case confirms it:
  **an eyebrow row should never carry a trailing icon whose size isn't derived from or pinned
  below the eyebrow text's own height** — any nav/expand affordance belongs on the content heading
  row instead, explicitly sized (see "Drivers' two independent tap targets" and Market Sentiment's
  chevron, both already this shape).
- **Second step of the rollout: Insights' Positioning screen** (`MarketPositioningView.kt`) had its
  3 group headers ("Retail Sentiment · AAII", "Institutional Positioning · CFTC COT", "Short
  Interest · FINRA") sitting *outside* their cards as a separate list item (`SectionLabel`,
  `titleSmall.Bold`, `onSurfaceVariant`), each followed by a `Spacer` then the card. Brought inside
  each card 2026-09-05 to match the section-title header pattern (`Macro Mix`/`Drivers`, etc.):
  replaced `SectionLabel` with `CardSectionHeader` (same `titleSmall.Bold` text, but now
  `primary`-colored like every section-title header on Summary, plus the full-bleed divider under
  it), rendered as the first child inside each of the 3 cards
  (`RetailSentimentCard`/`InstitutionalPositioningCard`/`ShortInterestCombinedCard`). Two of the
  three cards (`InstitutionalPositioningCard`, `ShortInterestCombinedCard`) already had no
  card-level `onClick` (each inner row is its own tap target), so the header just slots in above
  their existing content unchanged. `RetailSentimentCard` did have a card-level `onClick` covering
  its one content row — that had to move to a `Modifier.clickable` on the content `Row` itself
  (Drivers' two-tap-target pattern) so the new header's own info icon isn't swallowed by a
  whole-card tap target. Note this only touches the *group*-level header — each card's own
  per-metric content heading (e.g. "Bull – Bear Spread") is a separate, smaller piece of text
  already inside the content row and was left untouched.
- **Third step: Stock Detail's Digest and the Deep Dive screen.** 2026-09-05, two passes --
  the first was corrected by the second, both recorded here since the reasoning matters for the
  next screen.
  - **First pass (superseded):** converged `DeepDiveCard`/`DigestCard`'s headers onto Stock
    Detail's own pre-existing `SynthesisCardHeader` (`titleMedium.Bold`, icon sized to text --
    already used by `TechnicalRead`/`DeepStudy`/`Scenarios`), reasoning that a screen's own
    majority convention should win over importing Summary's family.
  - **Correction:** told explicitly that Digest and Deep Dive are "ai style cards" in the
    app-wide sense -- the same family as Market Signal/Market Sentiment/Indicators' Today's
    Read/Insights' Digest -- not local variants of Stock Detail's own `TechnicalRead`/`DeepStudy`/
    `Scenarios` convention, even though they happen to live on the same screen and share
    `PulseCardStyle.SYNTHESIS`. Sharing the SYNTHESIS *card style* doesn't imply sharing the same
    *header family* -- `SynthesisCardHeader` and `CardEyebrowLabel` are two independently-evolved
    header treatments that happen to both decorate SYNTHESIS cards; which one applies depends on
    whether the card belongs to Stock Detail's own older convention or the newer, cross-screen
    "ai style card" family this doc otherwise tracks. `DeepDiveCard`, `DigestCard`, and
    `DeepDiveSectionCard`'s kicker now use `CardEyebrowLabel` instead:
    - `DigestCard`'s "DAILY DIGEST" header and `DeepDiveCard`'s dated eyebrow line
      (`ui/screens/stocks/detail/sections/DigestCard.kt`,
      `.../sections/DeepDiveCard.kt`) both call `CardEyebrowLabel` with `iconRes =
      ic_ai_sparkle_filled` and `color = accentPrimary` -- matching Today's Read/Insights' Digest
      (both AI-narrative cards that carry the icon), not Market Signal/Sentiment's icon-less,
      `primary`-colored variant (those are computed-verdict cards, a different case per
      `CardEyebrowLabel`'s own doc comment on that split). `DeepDiveCard`'s text is dynamic (a
      dated string), so it's passed as-is -- `CardEyebrowLabel` uppercases its own text.
    - `DeepDiveSectionCard`'s kicker (`ui/screens/stocks/deepdive/DeepDiveSectionCard.kt`) now
      calls `CardEyebrowLabel` too, but **without** the icon -- this screen's own
      `DeepDiveHeaderBanner` already establishes "this whole screen is AI-generated" once, at the
      top, so each of the 8 section cards doesn't repeat it (same reasoning Summary's Lead
      Stories/Macro Mix/Watch/Risks entries don't each repeat an icon either).
    - `TechnicalRead`/`DeepStudy`/`Scenarios` were **not** touched -- only Digest and Deep Dive
      were named as needing to match the app-wide family; those three stay on Stock Detail's own
      `SynthesisCardHeader` convention (`titleMedium.Bold`) unless a future instruction says
      otherwise. This screen now deliberately carries two different SYNTHESIS-card header
      families side by side -- not yet resolved, see open item below.
    - `DeepDiveText.kt`'s compact `DeepDiveLabel` (shared with `StockPreviewCard`'s dense list
      row) and `DeepDiveHeaderBanner` (a pill-shaped `Surface`, not a `PulseCard`) were both left
      alone -- neither is the kind of card this correction was about.
  - Spacing/sizing fixes from the first pass carried forward unchanged (still correct under
    either header family): every SYNTHESIS card's header-to-content gap is `padding_medium`
    (fixed `DeepStudy`, `DigestCard`, `DeepDiveSectionCard`, all previously `padding_large` or
    `padding_small`); `DeepStudy`'s `Subsection` heading-to-body gap is `padding_medium` (was
    `padding_small`); `Scenarios`' two labelSmall captions ("What It Would Mean"/"What Would Need
    To Happen") got a `padding_medium` gap before their body text where there was previously
    none; `DeepDiveSectionCard`'s per-section `heading` is plain `titleMedium` (was
    `titleSmall.Bold`), matching Summary's list-entry convention (Lead Stories/Macro Mix/Watch/
    Risks).

  Not changed: `DeepStudy`'s 3 `Subsection` *titles* stayed `titleSmall.Bold` (a third-level label
  inside an already-headered card, closer to Drivers' own header than to a list entry's headline --
  no existing rule covers this case, flagging rather than guessing); `DigestCard`'s per-section
  `heading`/`body` (already a distinct, intentionally-muted per-entry kicker style, not the card's
  own header).

## The norm going forward: every SYNTHESIS card uses `CardEyebrowLabel`

Confirmed 2026-09-05, after the Third step correction above: `CardEyebrowLabel` (the app-wide
family -- Market Signal/Sentiment/Today's Read/Insights' Digest) is now **the standard header for
every `PulseCardStyle.SYNTHESIS` card in this app**, not a Summary-specific thing and not one of
two equally-valid options. Stock Detail's own `SynthesisCardHeader` (`titleMedium.Bold`) is a
legacy pattern being phased out card-by-card, not a parallel convention to keep maintaining --
don't reach for it on a new SYNTHESIS card. `TechnicalRead` moved onto `CardEyebrowLabel`
2026-09-05 (see Fourth step below); `DeepStudy` and `Scenarios` are still on the old pattern and
are being converged individually rather than in one sweep -- don't assume they're already done, and
don't convert them without being asked (owner is handling those directly).

## Fourth step (2026-09-05): TechnicalRead, six Stock Detail list-cards, and Direct News' header

- **`TechnicalRead`** moved from `SynthesisCardHeader` to `CardEyebrowLabel` (icon + `accentPrimary`,
  matching `DigestCard`/`DeepDiveCard`), per the norm above. Header→body gap was already
  `padding_medium`, unchanged.
- **Six Stock Detail sections that used to render as a page-level heading
  (`SectionDividerLabel`: `titleLarge`, `accentPrimary`, sitting directly on the background)
  followed by loose content, moved onto one `PulseCard(DATA)` each**, matching Macro Mix's
  structure: title inside the card (`titleSmall.Bold`, `primary`) + full-bleed divider, via a new
  shared `DataCardSectionHeader` (`ui/screens/stocks/detail/DetailSectionLabels.kt`, replacing
  `SectionDividerLabel` entirely -- it had no remaining call sites after this pass, so it was
  deleted rather than left dead).
  - `WatchList` ("What to Watch"), `Consider` ("Things to Check"), `SignalConditions`,
    `ForwardCalls`, `EventLog` ("Technical Timeline"): header + divider, then the section's
    existing entries, now living in the card's padded body instead of loose on the page
    background. **Per-row treatments were initially kept nested (superseded by the Fifth step
    below, same day) -- see that section for the corrected, final shape.**
  - **Resolved Calls** (`ForwardCalls.kt`'s `ResolvedCallsSection`) became its own separate
    `PulseCard(DATA)`, not folded into the Forward Calls card -- the two were always visually and
    functionally distinct (resolved is a collapsed-by-default historical record; open is the active
    list), so two cards instead of one merged card were the closer read of "put them all on cards."
    Its clickable header row (title + expand chevron) is scoped with `Modifier.clickable`, not
    `PulseCard`'s own `onClick`, so tapping content inside an expanded card never collapses it. The
    divider only renders when expanded (nothing to separate when collapsed). Still true after the
    Fifth step -- this one was never a nested-card issue, just two siblings.
  - **`SignalConditions`' `WhatChangedBox`** initially stayed its own separate `PulseCard(DATA)`
    sitting above the merged card (superseded by the Fifth step below, same day).
- **`DirectNews` (News tab) dropped its "DIRECT NEWS" header entirely** -- the tab itself is
  already titled "News," so the section-level restatement was redundant. Each news item still
  renders as its own separate `PulseCard(DATA)`, unchanged (this section was never a single merged
  card the way the other five are, and wasn't asked to become one). Still true after the Fifth
  step -- these are legitimately separate top-level cards, not nested ones.

## Fifth step (2026-09-05, same day as the Fourth): no cards nested inside cards, anywhere

Corrected immediately after the Fourth step: told explicitly that Stock Detail should have **no
cards (or card-like tinted boxes) nested inside another card** -- every card's content should be
either a single block or several blocks separated by a full-width divider, Macro Mix's structure,
with nothing that reads as its own smaller card living inside a bigger one. The Fourth step's
"keep per-row Surface treatments nested, that's a different concern" reasoning was wrong -- a
tinted `Surface` box wrapping a row's content is exactly the card-like nesting this rule targets,
regardless of whether it's a full `PulseCard` or a plain `Surface`. Fixed:

- **`WatchList`**: `WatchListRow`'s tinted `Surface` + HIGH-urgency colored left-border `Box` are
  gone. Each row is now a plain `Row` (just the urgency `SignalPill` + text), separated from its
  neighbors by a full-width divider inside the one "What to Watch" card. Urgency now reads entirely
  from the pill's color, with no separate left-rail highlight -- the same call Macro Mix itself
  made when it dropped its own accent-tinted left-rail earlier in this rollout.
- **`ForwardCalls`**: `ForwardCallCard` and `ResolvedCallCard` both lost their tinted `Surface`
  wrapper. Each is now a plain `Column` (unchanged content otherwise -- predicate/status pills,
  statement, dates), taking a `modifier` param so the caller can apply the per-entry
  `padding_large`; entries within each of the two cards (Forward Calls, Resolved Calls) are now
  separated by full-width dividers instead of a `spacedBy` gap between tinted boxes. The small
  predicate-label pill *inside* each entry is untouched -- that's a pill badge, not a nested card.
- **`Scenarios`**: `ScenarioCase`'s outer tinted `Surface` is gone. Bull and Bear each get their
  own padded block directly inside the "SCENARIOS" card, separated by a full-width divider when
  both are present (previously: two independently `Surface`-boxed cases with a plain gap between
  them, no divider). The case-label pill (BULL/BEAR) inside each block is untouched, same reasoning
  as Forward Calls' pill.
- **`SignalConditions`**: `WhatChangedBox` is no longer a separate `PulseCard` at all --
  its content (renamed `WhatChangedContent`, no `PulseCard` wrapper) is now the *first*
  divider-separated block inside the same "Signal Conditions" card, ahead of the category groups.
  This was a sibling-card case rather than literal nesting, but the end state is the same one-card,
  divider-separated structure as everywhere else, so it's included here rather than left as the
  one exception.

Not touched: `DirectNews`' per-item cards and `ForwardCalls`'/`Fundamentals`' small pill `Surface`s
(badges, not nested cards) -- see the Fourth step's own notes on those. `DetailSectionLabels.kt`'s
`OutlinedBadge` and `HighUrgencyAlertRow` are also plain badges/standalone banners, not nested
inside another card, so out of scope here too.

## Open items for the next pass (not yet decided — don't assume an answer)

1. **Content-heading weight** (Signal/Sentiment bold vs. the four list cards plain) — intentional
   split by card role, or should it be unified alongside size?
2. **Which-family-for-a-new-card rule** (section-title vs. eyebrow, above) is inferred from the
   current 12 cards, not confirmed by whoever owns the design system.
3. **`theming-spec.md` §8** still only describes the older DATA/SYNTHESIS title-tier table and
   doesn't mention either header family here — worth reconciling once these conventions are
   confirmed stable, so there's one authoritative typography section instead of two.
4. **Everything above "Resolved" is Summary-only; the rollout to other screens is tracked in
   "Resolved" as it happens** — as of 2026-09-05: Indicators' "Today's Read" and Insights' "Digest"
   (step 1, spacing/icon-sizing only), Insights' Positioning (step 2, group headers moved inside
   their cards), and Stock Detail's Digest + the Deep Dive screen (step 3, converged onto
   `CardEyebrowLabel` — the app-wide "ai style card" family — not Stock Detail's own
   `SynthesisCardHeader`). News, and the DATA-style cards on Stocks/Indicators/Insights, are still
   untouched — don't treat a pattern documented here as already applied to a screen or card family
   not named in "Resolved."
5. **Stock Detail still runs two SYNTHESIS-card header families side by side, shrinking one card
   at a time**: `DeepStudy`/`Scenarios` remain on the screen's own `SynthesisCardHeader`
   (`titleMedium.Bold`); `TechnicalRead`/`DigestCard`/`DeepDiveCard`/`DeepDiveSectionCard` are now
   on `CardEyebrowLabel` (`labelSmall`, the Market Signal family and, per the norm section above,
   the standard for every SYNTHESIS card going forward). The owner is converting the remaining two
   individually rather than in one sweep — don't convert `DeepStudy`/`Scenarios` without being
   asked, and don't assume they're done just because the norm is written down.
