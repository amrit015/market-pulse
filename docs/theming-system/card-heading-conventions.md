# Card heading conventions — piloted on Summary, rolling out screen by screen

> **Status: LIVING / IN PROGRESS.** This documents a set of card-heading/spacing conventions
> established on the Summary screen (`ui/screens/summary/views/SummaryScreen.kt`) during an
> initial 2026-09-05 pass, with the explicit intent to roll the same conventions out to other
> screens once they're settled. As of 2026-09-06 that rollout is underway (see "Resolved" below for
> exactly what's landed on Indicators/Insights/Stock Detail/Deep Dive so far, including six Stock
> Detail sections moved from a page-level heading onto one `PulseCard` each, and Indicators' own
> pillar tabs + Horizons cards) but still partial -- most of News, and several DATA-style cards
> elsewhere, are untouched. Several open questions are called out explicitly at the bottom rather
> than silently resolved — read those before extending this pattern to a new screen. Verified
> against source on 2026-09-06; re-check line numbers before citing them if this file is read much
> later.

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

## Sixth step (2026-09-06): Indicators' pillar tabs, its Horizons cards, and a rejected line-height experiment

- **Indicators gained real tabs** (`IndicatorsScreen.kt`) -- the 4 pillars (Tactical Momentum,
  Systemic Risk, Valuation, Macro Vitals) that used to stack in one long scroll are now one
  `PulseTabRow` tab apiece, swipeable via `HorizontalPager`, same shape Insights already used for
  its own 4 sections. Tab *labels* are short (`indicators_tab_momentum` = "Momentum",
  `indicators_tab_systemic_risk`, `indicators_tab_valuation`, `indicators_tab_macro` = "Macro") and
  deliberately distinct from the `pillar_*` strings each tab's own inner section heading still uses
  ("Tactical Momentum", "Macro Economy") -- a short nav label vs. a longer page title, not a
  duplication to fix. The "Analyzed as of" timestamp, Today's Read (the AI executive briefing
  card), and the Horizons nav card all stay shared chrome above the tab row rather than becoming
  tab-specific content -- see `docs/architecture/collapsing-header-tabs.md` for the scroll/pager
  mechanics that make that chrome collapse-then-stick, which is a separate concern from this doc's
  own card/heading/spacing scope.
- **Horizons' per-timeframe cards** (`HorizonCard`, `IndicatorHorizonsScreen.kt`) converged onto
  the same `CardEyebrowLabel` family as every other AI-style card: the hand-rolled
  `labelMedium`/`onSurfaceVariant` time-window row (no icon) became `CardEyebrowLabel` (icon +
  `accentPrimary`); the risk pill moved off the eyebrow's own row onto its own row directly below
  it (an eyebrow row never carries trailing content beside its own icon in this convention -- see
  Today's Read's alignment pill, which sits below its eyebrow the same way); `posture` (this card's
  own AI-authored headline) dropped from `titleLarge` to `titleMedium.Bold`, matching every other
  AI-card's content heading; every heading-to-body gap is `padding_medium` now (`posture` →
  `whatThisMeans`, and `whatThisMeans` → `watchFor`, were both `padding_large`).
- **`HorizonNavigationCard`** (the "Horizons" entry-point card on Indicators' own main feed, not to
  be confused with the per-timeframe `HorizonCard`s above) had a `padding_tiny` (2dp) gap between
  its title and subtitle -- a leftover from before it became a `PulseCard(SYNTHESIS)`, never caught
  until this pass. Fixed to `padding_medium`, matching every other AI-card's heading-to-body gap.
- **A proportional line-height boost (`lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
  * 1.2f`) was tried on several AI-cards' body text and then explicitly rejected** -- present before
  this session on `AiExecutiveBriefingHero`'s `whatChanged` and the pillar scorecard's `oneLiner`;
  added to `HorizonCard`'s `whatThisMeans`/`watchFor` and (briefly) `HorizonNavigationCard`'s
  subtitle during this same pass, reasoning it should match those two. The owner then removed the
  multiplier from all of them, back to each style's own plain default line height -- **this was a
  deliberate design call, not an oversight to "fix" back in**. If a future card is tempted to add
  `* 1.2f` (or any other line-height multiplier) to match an older card that still has it, treat the
  multiplier itself as the outlier to remove, not a pattern to propagate -- plain default line
  height is the settled convention for AI-card body text app-wide now.

## Seventh step (2026-09-06): Deep Study converged onto Scenarios, no AI-sparkle icon on Stock Detail, a universal "see more" CTA, and a shared bullet-join helper

- **`DeepStudy` restructured onto `Scenarios`' merged-card shape**, per explicit instruction to
  match the two. Was one padded `Column` holding the header plus all 3 subsections back-to-back
  with a trailing `Spacer` after each (including the last); now the header sits in its own padded
  block, followed by a full-width `HorizontalDivider`, then each subsection in its own padded block
  separated by a full-width divider (skipped after the last one) — identical shape to `Scenarios`'
  Bull/Bear blocks. Each subsection's title also converged onto `Scenarios`' kicker style
  (`labelSmall`/`accentPrimary`) from its previous `titleSmall.Bold`/`onSurface` — this resolves
  open item #5 below's note that `DeepStudy`'s subsection titles were an unconverged third case;
  they're now the same per-entry-kicker treatment every other card in this family uses. (`DeepStudy`'s
  own *card* header still calls `SynthesisCardHeader`, unchanged — this step only touched the
  subsections inside it, not the card-level header family; see the note on the icon removal below
  for what did change about `SynthesisCardHeader` itself.)
- **No AI-sparkle icon anywhere on the Stock Detail screen — a Stock-Detail-specific exception to
  the app-wide "AI-narrative cards carry the icon" convention** described under "Eyebrow header"
  above. Reasoning: every card on this screen mixes quant data and AI-authored narrative, so a
  sparkle singling out "the AI card" among siblings that are equally AI-touched is misleading in a
  way it isn't on Summary/Indicators/Insights (where a genuine DATA-vs-AI card split exists).
  Concretely: `SynthesisCardHeader` (`ui/screens/stocks/detail/DetailSectionLabels.kt`) dropped its
  hardwired `Icon` entirely — it's now just the `titleMedium.Bold`/`accentPrimary` text, no icon
  parameter at all (it never took one; the icon was baked in, so this is a body change, not a
  signature change). This affects both of its remaining callers, `DeepStudy` and `Scenarios`.
  `DigestCard` (`ui/screens/stocks/detail/sections/DigestCard.kt`) dropped `iconRes =
  ic_ai_sparkle_filled` from its `CardEyebrowLabel` call for the same reason. **Not** touched:
  `DeepDiveCard`/`DeepDiveLabel`/`DeepDiveHeaderBanner` all still carry an icon, but it's
  `ic_deep_dive` — a feature glyph marking "this opens/is the Deep Dive feature," not the general
  "AI-authored content" sparkle, so it isn't the icon this exception is about. If a future Stock
  Detail card is tempted to add `ic_ai_sparkle_filled`, treat that as the thing to question, not a
  gap to fill in.
- **`ViewMoreRow` (`ui/screens/stocks/detail/DetailSectionLabels.kt`) generalized into this app's one
  universal "see more" CTA** — added a required `text: String` param (was hardcoded to "View More").
  Any affordance that *navigates* to more content (a fuller list on its own screen, a full feature
  screen) should render as `ViewMoreRow(text = ..., onClick = ...)` rather than hand-rolling its own
  `Row { Text(labelMedium.Bold, accentPrimary); Icon(ic_chevron_forward, accentPrimary) }` — Resolved
  Calls' and Technical Timeline's "View More" links and Deep Dive's "Open full Deep Dive" CTA
  (`DeepDiveCard.kt`) all route through it now. This does **not** cover in-place expand/collapse
  toggles (an up/down-arrow chevron, not a forward chevron) — those are a different interaction
  model (accordion, not navigation) and keep their own hand-rolled affordance (Resolved Calls' own
  header row, Technical Timeline's own header row, `DirectNews`' "Read the analysis" toggle).
- **A shared bullet-join helper, `buildBulletJoinedText` (new file
  `ui/components/widgets/BulletText.kt`), plus a new `R.string.bullet_separator` ("•") resource.**
  Every literal `"•"` hardcoded inline in a Kotlin string (`SimpleDateFormat` patterns,
  `joinToString(" • ")`, plain string templates) moved onto this: `Consider.kt`'s bulleted-list
  marker, `NewsScreen.kt`'s "· {relative time}" caption, `WeeklyPlaybookView.kt`'s event date/time
  line (its `formatEventDateSafe` helper was split to return date and time as separate parts instead
  of one pre-joined string, since the join now has to happen in a composable that can resolve the
  string resource), and every Deep Dive touchpoint (see below). The helper renders the bullet
  glyph itself ~1.4× the surrounding text's font size via an `AnnotatedString` `SpanStyle` — a
  plain-size bullet next to normal text read as a low, easy-to-miss dot. It's plain (non-
  `@Composable`) so it can't reach into `stringResource`/`MaterialTheme` itself; callers resolve
  `bullet`/`baseFontSize` and pass them in. **Not** used for the app's other, pre-existing `" · "`
  (middle-dot, U+00B7) joins scattered across many screens (`"{name} · {symbol}"`-style captions)
  — those are a visually different, more common glyph and weren't in scope for this pass; don't
  conflate the two characters if extending this further.
- **Deep Dive's date/status text, unified across all three touchpoints** (`DeepDiveCard`'s chrome
  card, `DeepDiveLabel`'s compact preview-card row, `DeepDiveHeaderBanner`'s screen banner) into one
  format: `(icon) DEEP DIVE AVAILABLE, {date} • NEXT: {date}` (cold start, no deep dive yet:
  `NEXT DEEP DIVE: {date}` alone). The label half of each piece (`"DEEP DIVE AVAILABLE, %1$s"`,
  `"NEXT: %1$s"`, `"NEXT DEEP DIVE: %1$s"`) is now **pre-uppercased in the string resource itself**,
  not runtime-`.uppercase()`'d at render time — the interpolated date must stay natural case
  ("Sept 4", not "SEPT 4"), which a blanket `.uppercase()` on the whole composed string can't do.
  Because of this, `DeepDiveCard` no longer routes its dated line through `CardEyebrowLabel` (which
  always calls `.uppercase()` on its full `text` — would re-uppercase the date too); it hand-rolls
  the same icon-sized-to-text-height technique directly instead. `deepDiveDisplayParts`
  (`ui/screens/stocks/detail/sections/DeepDiveText.kt`) is the one shared source for the compact
  (no-year) date parts, consumed by `DeepDiveLabel` and `DeepDiveCard`; `DeepDiveHeaderBanner` keeps
  its own separate parts (it needs the with-year date format for its more prominent, standalone
  banner) but follows the same pre-cased-parts-joined-via-the-bullet-helper shape. The old
  `deepDiveDisplayText` (a plain-`String`-returning wrapper, needed only for the now-removed
  `CardEyebrowLabel` call site) was deleted as dead code. **Superseded the same day -- see the
  Ninth step below**: the three touchpoints no longer share one identical format, and the
  "banner" described here doesn't exist anymore.
- **`DirectNews`' headline/body divider now stretches the card's full width.** It used to sit inside
  one uniformly-`padding_large`-padded `Column` spanning the whole card's content, so the divider
  itself was inset by `padding_large` on both sides instead of reaching the card's edges. Fixed by
  splitting that one `Column` into two padded blocks (impact/date + headline/chevron; then
  synthesis/expanded-detail/read-the-analysis) with the divider sitting at the outer, unpadded level
  between them — the same "divider outside any padded container" idiom every merged-card section
  already uses (see "The merged card pattern" above). Its `forwardImplication`/
  `transmissionMechanism` kickers also moved from `onSurfaceMuted` to `accentPrimary`, matching
  every other per-entry kicker in this family (see the Eighth step below for the fuller kicker
  convention writeup).

## Eighth step (2026-09-06, same day): per-entry kicker sub-labels are ALL CAPS, and match the card's own header color

Generalizing a pattern that had been applied piecemeal (Scenarios' "WHAT IT WOULD MEAN"/"WHAT WOULD
NEED TO HAPPEN" were already caps; `SignalConditions`' category labels were already caps; `DeepStudy`'s
3 subsection titles and `DirectNews`' 2 expanded-detail labels were not) into one explicit rule for
this whole family — the small `labelSmall`/`labelMedium` caption sitting directly above an entry's
own body text, inside an already-headered card (Digest's per-section heading, Scenarios' two kicker
labels, Deep Study's 3 subsection titles, Signal Conditions' category labels, Direct News' forward
implication/transmission mechanism labels):

- **All caps.** `DeepStudy`'s `stock_detail_what_the_numbers_say`/`stock_detail_valuation_context`/
  `stock_detail_macro_impact` and `DirectNews`' `stock_detail_forward_implication_label`/
  `stock_detail_transmission_mechanism_label` string resources were re-cased to literal caps in the
  XML (`"WHAT THE NUMBERS SAY"`, not `"What the numbers say"`) — matching the established approach
  every other static kicker string already used (pre-uppercased in the resource, no runtime
  `.uppercase()` call), rather than mixing in a second, code-side transform. `DigestCard`'s
  per-section heading is the one exception that still calls `.uppercase()` at the render site
  (`section.heading.orEmpty().uppercase()`) — its text is model-generated, not a fixed resource, so
  there's nothing to pre-case in XML.
- **Color matches the card's own header, not a blanket single color.** Confirmed/re-stated from the
  color pass two sessions ago: a card built on `CardEyebrowLabel`/`SynthesisCardHeader` (both
  `accentPrimary`) gets `accentPrimary` kickers — Digest, Scenarios, Deep Study, Direct News (whose
  own card has no header of its own, but was explicitly asked to match this family anyway). A card
  built on `DataCardSectionHeader` (`colorScheme.primary`) gets `colorScheme.primary` kickers —
  Signal Conditions' category labels, Summary's Domino Effect timeline labels. Don't default a new
  kicker to `accentPrimary` without checking which header family its own card actually uses.

## Ninth step (2026-09-06, later the same day): Deep Dive's three touchpoints diverge on purpose, and the screen banner is gone

Corrects the Seventh step's "unified across all three touchpoints, one identical format" framing --
that lasted about one turn before the owner asked for each touchpoint to say something different,
suited to its own context. The one thing that's still shared: `"DEEP DIVE AVAILABLE, ..."` /
`"NEXT: ..."` / `"NEXT DEEP DIVE: ..."` stay the label halves, pre-uppercased in their string
resources, date natural case, per the Seventh step's reasoning -- only *which* touchpoints combine
them, and which date format each uses, changed.

- **Preview card (`DeepDiveLabel`)**: full month name (`toLongDateString()`, "September 4"), and
  shows **exactly one** of the two states, never both joined -- `"DEEP DIVE AVAILABLE, September 4"`
  when a deep dive has run (even if a next-trigger date also exists -- the next date is dropped
  entirely in that case, not appended), or `"NEXT DEEP DIVE: September 18"` alone at cold start.
  New `deepDivePreviewText` (`ui/screens/stocks/detail/sections/DeepDiveText.kt`) is this exact
  two-state branch -- deliberately not `deepDiveDisplayParts` (which returns *all* present parts
  for the joined-together case), and no longer needs the bullet-join helper at all since there's
  only ever one part to render.
- **Stock Detail chrome (`DeepDiveCard`)**: unchanged from the Seventh step --
  `"DEEP DIVE AVAILABLE, Sep 4 • NEXT: Sep 18"` (abbreviated month, `deepDiveDisplayParts`, both
  parts joined when both are present) or `"NEXT DEEP DIVE: Sep 18"` alone at cold start.
- **Deep Dive screen -- no more banner.** The pill-shaped `accentPrimary`-on-`accentSurfaceStrong`
  `Surface` with the `ic_deep_dive` icon (previously `DeepDiveHeaderBanner`) is gone entirely,
  replaced by a plain caption line -- `labelSmall`/`onSurfaceMuted`, no icon, no colored container --
  matching the same "Analyzed as of" treatment `DetailHeader`'s own `analyzedAsOfTimestamp` line and
  the shared `AnalyzedAtHeader` (Indicators/Summary) already use elsewhere: this is fundamentally
  the same "here's when this was last updated" fact every other screen states plainly, not a thing
  that needed its own more prominent chrome. Its label also changed from `"DEEP DIVE AVAILABLE, ..."`
  to `"Updated as of ..."` (sentence case, no eyebrow caps) -- this screen already establishes "this
  is the Deep Dive" by being it, so restating "DEEP DIVE AVAILABLE" read as redundant in a way it
  doesn't on the preview card or Stock Detail's own chrome. Renamed `DeepDiveHeaderBanner.kt` →
  `DeepDiveUpdatedAtLine.kt`, composable `DeepDiveHeaderBanner` → `DeepDiveUpdatedAtLine`, to match
  what it actually is now. Still shows `"Updated as of Sep 4, 2026 • NEXT: Sep 18"` (with-year,
  own separate parts, same bigger-bullet join) when available; cold-start behavior (next-only,
  standalone) is unchanged.
- **Consequence for `DeepDiveSectionCard`'s per-section kicker (no icon):** its doc comment used to
  justify the missing icon by pointing at `DeepDiveHeaderBanner` "establishing this whole screen is
  AI-generated once, at the top" -- with the banner (and its icon) gone, that reasoning no longer
  holds. Re-justified instead by the same reasoning as Stock Detail's own no-AI-icon exception
  (Seventh step, above): the Deep Dive screen mixes quant and AI content throughout, so no single
  card should carry a sparkle singling itself out. The *outcome* (no icon) didn't change, only why.

## Tenth step (2026-09-06, later still): `StatGrid` becomes the formal metric-grid convention

Confirms what `ui/screens/stocks/detail/StatGrid.kt`'s own doc comment had flagged as not yet
written down: **any card rendering a row of label+value stat pairs (Fundamentals, Macro,
MomentumAndTrend, Returns, HeadlineMetricsStrip, KeyLevels) uses `StatGrid(stats, columns = 3)`,
not a hand-rolled `FlowRow` of fixed-width cells or a bare `Row(SpaceBetween)`.** Both of those
older patterns broke the same way: a fixed 96dp cell (`detail_stat_width`) reads fine for a short
number, but cramps or wraps a value that's a full word or a min–max range (Macro's Rate Sensitive
value, e.g. "MODERATE NEGATIVE"; Fundamentals' 5Y PE range), and a lone leftover stat in the last
row sat narrow and left-aligned with empty space beside it instead of using the row it had to
itself. `StatGrid` fixes both: every cell in a row gets `Modifier.weight(1f)` instead of a fixed dp
width, so a full row of 3 divides evenly and a partial last row (1 or 2 items) still divides *that
row's* width evenly among however many are in it. `KeyLevels` calls it twice with `columns = 2`
(resistance/support, then fairValueAnchor/valueArea) rather than once with a flattened list, to
preserve its two independent stat pairings rather than re-chunking across a null field. See
`CardStyleShowcase.kt`'s "2. DATA — Stat grid" sample, which also demonstrates the space-around-
dash fix below.

**Related, separate fix applied to every range value in these same cards:** a `"$X–$Y"` string with
no space anywhere gives Android's text layout no valid line-break point, so a column too narrow to
fit the whole range wraps mid-number (e.g. `"$598."` / `"17"` on two lines) instead of at the dash.
Fixed by writing `"$X – $Y"` (a plain space on each side of the en dash) everywhere a range value is
built — not a font-size or `maxLines` fix, purely giving the layout a valid place to wrap.

## Eleventh step (2026-09-06, later still): Market Signal's regime/direction split, tappable headline, and continuous conviction meter

`SignalSection` (Summary's top card) changed in three ways, all still `PulseCardStyle.SYNTHESIS`:

- **`regime` and `direction` are now two separate pills, not one.** They used to share a single
  chip — `regime`'s text, tinted and arrow-marked by `direction`'s color — with no separate text for
  direction at all. Now: `regime` renders as an **outlined**, neutral (`onSurfaceMuted`) pill (it's
  a classification — which of 6 phases the model thinks we're in — not a bullish/bearish read on its
  own); `direction` (RISK ON/RISK OFF/MIXED) renders as a **filled**, signal-colored pill, right next
  to it. Each is its own tap target (trailing chevron), opening a glossary sheet scoped to just that
  one term — regime and direction no longer share one combined sheet (see the Twelfth step below).
  The direction pill briefly also carried a leading ▲/▼/▪ glyph (the same convention
  `DriversSection`/`ScoreGauge` use) but that was dropped the next day (2026-09-07) — redundant once
  the pill text already says "RISK ON"/"RISK OFF"/"MIXED", and the MIXED glyph in particular read as
  a stray dot rather than a meaningful mark.
- **The flash headline (`signalLine`) is now tappable.** The whole headline row (text + trailing
  chevron, not just the chevron icon — fixed 2026-09-07, the icon-only tap target was too small)
  opens a new `MarketReadBottomSheet` (`ui/components/bottomSheet/MarketReadBottomSheet.kt`) showing
  the headline plus the same `analysis`/`posture` prose `TheReadSection` renders at the bottom of the
  page — a shortcut for the reader who wants the "why" without scrolling past drivers/position/
  stories/macro/domino/watch/risks to reach it. `TheReadSection` itself is unchanged and still
  renders in its usual place; this is an additional entry point, not a replacement. The sheet's own
  headline recap runs through `smartTitleCase()` too (fixed same day — it hadn't, so the same
  `signalLine` value showed title-cased on the card and raw-cased in the sheet it opens).
- **Conviction meter is now a continuous bar, not 3 discrete segments.** Matches
  `CardStyleShowcase.kt`'s "11. SYNTHESIS — Headline + divider + meter" sample (which is what this
  redesign was modeled on, in the reverse direction from usual): the "CONVICTION" label sits on its
  own line above a full-width track, filled left-to-right by a fraction (LOW = 1/3, MODERATE = 2/3,
  HIGH = full) in the same `directionColor` the direction pill uses. Reads relative strength at a
  glance rather than in 3 blocky steps. **Track color is `colorScheme.onSurface.copy(alpha = 0.12f)`,
  not `colorScheme.surfaceVariant`** (fixed 2026-09-07) — this card's own SYNTHESIS background
  (`accentSurfaceStrong`) reads close enough in value to `surfaceVariant` in dark mode that the empty
  track was indistinguishable from the card behind it. A translucent overlay of the foreground color
  guarantees contrast against whatever background sits behind it, regardless of card style or theme
  preset — the same technique the old 3-segment meter's own "empty" segments already used, don't
  reach for a flat surface token here again.

`smartTitleCase()` (`utils/extensions/StringExtensions.kt`, added earlier the same day for the
Digest/Indicators/Insights AI headlines — see `compose-conventions.md`'s "AI-headline title casing"
section) was also extended to `signalLine` and Market Sentiment's `headline` — the same normalized
Title Case treatment, not a new rule.

## Twelfth step (2026-09-06, later still; corrected 2026-09-07): the glossary bottom sheet's term list moves onto cards, matching Indicators' Bands exactly

`MarketGlossaryBottomSheet` (`ui/components/bottomSheet/MarketBottomSheet.kt`) — the sheet opened by
tapping a regime/direction/setup/cycle-zone/stock-setup badge anywhere in the app — was a plain
`Column` per term with a tinted background on the current one. Redesigned to match the "list of
terms, one of them is the current reading" pattern Indicators already has, at
`MetricDetailScreen.kt`'s `BandRow` (its own "BANDS" section) — the **canonical** version of this
pattern in the app, not `GlossaryDetailScreen.kt`'s `GlossaryBandRow` (Positioning's own
glossary-detail page), which the first pass on 2026-09-06 was modeled on instead and turned out to
diverge from `BandRow` on both spacing and card content. Caught by the owner comparing the two
directly the next day; re-checked against `BandRow` line by line and fixed:

- **Section title**: `labelMedium.Bold`, `colorScheme.primary`, `padding(bottom = padding_medium)`
  only (no top padding) — not `labelSmall`/split top-and-bottom padding, which is what the
  `GlossaryDetailScreen`-modeled first pass had.
- **Every term is its own `PulseCard(DATA)`**, not a plain background-tinted `Column`. The current
  term gets a `border_thin` accent-colored ring layered on top of the card's own border (via
  `.border()`, since `PulseCard` locks its own border color) — not a background tint.
- **The term label is always `colorScheme.onBackground`**, current or not — it does NOT re-color to
  accent when current (the first pass did; `BandRow` doesn't).
- **"Current" is a `SignalPill`** (`pillColor = accent.copy(alpha = 0.16f)`, `contentColor = accent`,
  text from `R.string.status_current`) sitting opposite the term name — not a plain accent-colored
  "CURRENT" text label, which is what the first pass had (and what `GlossaryDetailScreen`'s
  `GlossaryBandRow` still does — that page was **not** touched in this fix; it's a known, separate
  divergence from `BandRow`, flagged but not silently changed since nobody asked for it yet).
- **Smaller type throughout.** `titleMedium`/`bodyMedium` dropped to `titleSmall`/`bodySmall` for
  both the term cards and the "Current Verdict" rows — several term cards stack in a limited-height
  sheet here, unlike a full page, so the larger sizes read as oversized for the amount of content on
  screen at once. This part of the first pass was correct and unchanged.
- **"Current Verdict" is now its own `PulseCard(DATA)`** (header + full-bleed divider + padded rows
  — the same section-title header shape every other data card in the app uses, see the "Two header
  families" section above) instead of a headerless block of plain text sitting above the term-list
  cards. Content unchanged (still one row per `currentX` param that's non-null); it just reads as a
  card among cards now, not a stray block. Also unchanged in the fix pass.

See `CardStyleShowcase.kt`'s "14. Emphasis border overlay (current selection)" sample, updated to
cite `BandRow` as the one canonical source rather than listing all three consumers as equally
authoritative.

## Thirteenth step (2026-09-07): the glossary sheet's own background took two tries to get right

Once term cards moved onto `PulseCard(DATA)` (Twelfth step), `MarketGlossaryBottomSheet`'s own
`ModalBottomSheet` background needed to change too — the cards it now nests turned out to expose
that this app's `SurfaceRamp` (`PulseTokens.Color.kt`) only has 3 genuinely distinct values
(`background`, `surface`, `surfaceElevated`), and every bottom sheet in the app had been defaulting
to `surfaceContainerHighest`, which this app's `MarketPulseTheme.kt` maps onto the literal same
value as `surfaceVariant` (`surfaceElevated`) — also `PulseCard`'s DATA fill color. Two tokens were
tried and rejected before landing on the right one:

1. **`surfaceContainerHighest`** (what every bottom sheet already used) — a DATA card nested inside
   has *zero* contrast against the sheet, since both resolve to `surfaceElevated`. This is what
   prompted the Twelfth step's redesign to even surface the problem.
2. **`colorScheme.background`** — fixes the card-vs-sheet contrast (DATA cards are designed to sit on
   `background` everywhere else, via shadow + accent-tinted background), but is the literal same
   value the *screen behind the sheet* already uses, so the sheet itself lost its own boundary —
   hard to tell where the sheet starts and the page ends. Caught by the owner immediately after the
   first fix shipped.
3. **`colorScheme.surface`** (landed) — the one remaining `SurfaceRamp` value, genuinely distinct
   from both `background` and `surfaceElevated` (dark mode: `0xFF17181D` sits between background's
   `0xFF0D0E12` and surfaceElevated's `0xFF1F2026`). Satisfies both constraints at once: the sheet
   reads as its own layer against the screen, and a DATA card nested inside still reads against the
   sheet. Notably, `colorScheme.surface` was deliberately *avoided* as a `TopAppBar` containerColor
   elsewhere in the app (see `SettingsScreen.kt`'s own comment) for reading "noticeably different"
   from `background` — that's a bug for a top bar meant to blend seamlessly with the page below it,
   but it's exactly the property a modal sheet needs. Same token, opposite intent, both correct in
   their own context — don't treat one usage as precedent against the other without checking why.

Only `MarketGlossaryBottomSheet` was changed — it's the one bottom sheet in the app that nests
`PulseCard` content (see Twelfth step); `DriversInfoBottomSheet`/`RiskBottomSheet`/
`StockAnalysisGlossaryBottomSheet`/`MarketReadBottomSheet` render plain text only, so
`surfaceContainerHighest` still works fine for them (no card fill to collide with) and none were
touched.

## Fourteenth step (2026-09-07; layout corrected later the same day): Deep Dive sections gain a hero stat + a highlights grid, `StatGrid` gains `note`

Backend As-Built Update #2 (companion to the already-implemented `spec-20260904-deep-dive-expansion.md`)
added two new fields to every Deep Dive section — `headline_stat: {label, value} | null` and
`highlights: [{label, value, note?}]` (always an array, 2-5 items typical) — plus a 9th topic,
`WHAT_MOVES_IT` (between `CURRENT_STANDING` and `NEAR_TERM_OUTLOOK` in the backend's own fixed
order; the client renders by array order / `topic` match, never a fixed index, so no ordering logic
needed updating). Confirmed against backend source (`deepDivePrompt.ts`) before implementing, not
assumed from the spec doc alone — the doc's schema notation matched exactly, but nullability details
(`headline_stat` is an explicit `null`, never omitted; `highlights` is always `[]`, never `null`/
omitted; `note` is genuinely optional) needed the source check to get the Moshi types right.

- **`headline_stat` placement, corrected same day**: originally landed as its own block directly
  under the kicker, before the heading (reasoning: the hero number reads before the section's own
  AI-written heading). The owner flipped this after seeing it live — moved to AFTER the heading+body
  instead, so the narrative reads first and the supporting figure follows. Final stacking order:
  kicker → heading → body → `headlineStat` → `highlights` → `deltaChips`.
- **An inset `HorizontalDivider`** (inside the card's own content padding, not full-bleed
  edge-to-edge like a card's header divider) now separates each of the 3 populated blocks
  (heading+body / `headlineStat` / `highlights`) from the one before it — never inserted next to an
  absent block, so a section with only `highlights` gets exactly one divider, not two. New shared
  `InsetSectionDivider` composable in `DeepDiveSectionCard.kt` (Spacer/Divider/Spacer, same
  `onSurface.copy(alpha = 0.1f)`/`border_thin` treatment every divider in the app uses).
- **`headlineStat`'s value uses `colorScheme.primary`**, not `onSurface` — the one hero number this
  section calls out reads as emphasized, distinct from every other plain value on the card.
- **`highlights`** renders via the shared `StatGrid` (see Tenth step) — the same equal-width
  metric-grid convention Fundamentals/Macro/etc. already use, now also backing a second, unrelated
  screen (Deep Dive). `StatGrid`'s `StatItem` gained an optional third field, `note: String?`,
  rendered as a smaller, further-muted (`onSurfaceMuted.copy(alpha = 0.7f)`) line under the label —
  every other `StatItem` call site leaves it null and is unaffected.
- **`FundamentalsDeltaChips` (the WHATS_CHANGED-only pills after `highlights`) stack one per line**
  now, a plain `Column` instead of a wrapping `FlowRow` — each delta's full "{label}: {from} →
  {to}" text ran long enough that letting chips wrap crowded together with ragged leftover space
  instead of each getting a clean full-width line.
- **No severity/color inference.** Neither field ever carries a severity/sentiment from the backend
  — label/value/comparison only. `DeepDiveSectionCard` doesn't tint or icon-mark anything based on a
  highlight's presence or wording; any future visual emphasis has to come from the app's own
  deterministic logic on a field it already has elsewhere (matching this app's existing rule for
  `setup`'s color, see the Eleventh step's sibling reasoning on `SignalSection`).
- **Room needed no migration** — `sections` is a JSON-blob column (Moshi reflection adapter via
  `StocksConverters`), so new nullable fields on the nested `DomainDeepDiveSection` just change what
  the same column's JSON contains, not its SQL shape.

See `CardStyleShowcase.kt`'s "12. SYNTHESIS — Kicker + stat + heading + highlights + delta chips"
sample, extended to show all three optional blocks (headline stat, highlights grid, delta chips)
stacked in one card — a demonstration of the card's full possible range, not a claim that a real
section carries every block at once.

## Fifteenth step (2026-09-07): `setup_confirming`/`setup_conflicting` gain a `meaning` field, tap-to-expand

Backend change (`54c2f93`, 2026-09-06) moved Stock Detail's `setup_confirming`/`setup_conflicting`
off plain `string[]` onto the same structured `{label, meaning, direction}` shape `setup_signals`
already used — explicitly "to match `condition_labels`' explanatory style" per the backend's own
commit message. Confirmed against backend source before implementing (not assumed from a report
alone): `classification.ts`'s `SetupSignal` interface and `stockAnalysisEngine.ts`'s write path.

Caught mid-change: the Kotlin-side `NetworkSetupSignal`/`DomainSetupSignal` types already existed
(added in an earlier pass for `setup_signals`) but were missing the new `meaning` field entirely —
so this fixed two gaps at once: `setup_confirming`/`setup_conflicting` moving off `List<String>?`,
and `SetupSignal`'s own shape catching up to what the backend actually sends.

`SetupReasoning.kt`'s `ReasoningRow` mirrors the exact interaction `SignalConditions.kt`'s
`CategoryGroup` already uses for `condition_labels`' own `meaning` field (see the Seventh step) —
tap the whole row to toggle `meaning` open as a smaller, muted caption beneath it, rather than
always showing it. `label` keeps rendering in the row's existing position (no visual regression for
someone who never taps); the caption indents to align under the reasoning text column, not the
bar/label columns, so it reads as elaborating on that text specifically.

**Correction, same day:** this was first shipped on the assumption that no Room migration was
needed, since `setupConfirming`/`setupConflicting` already had `TEXT` columns and Room resolves
`@TypeConverters` by Kotlin type database-wide (see `StocksConverters.kt`'s own doc comment) — true
for the SQL schema, but wrong about existing cached *content*. A real crash proved it:
`JsonDataException: Expected BEGIN_OBJECT but was STRING at path $[0]` in `toSetupSignals`, on a
device whose cached `market_stock_details` rows still held the old `["reason one", "reason two"]`
plain-string JSON from before this change -- Room's schema validation checks column names/types,
not blob contents, so the mismatch only surfaced at decode time. Fixed with `MIGRATION_24_25`
(version bump 24 → 25), which `UPDATE`s both columns to `NULL` on upgrade rather than trying to
transform the old shape in SQL -- the next `refreshDetail` repopulates them correctly from the
network, same as a symbol whose detail was never cached. **Lesson for any future field-shape
change that reuses an existing TEXT/JSON-blob column** (not just adding a new column): a migration
is still needed even though the SQL column type doesn't change, specifically to clear or transform
existing rows' now-incompatible JSON content -- "the column already exists as TEXT" is not the same
guarantee as "existing rows' JSON already matches what the new type expects."

**Second correction, next day:** the owner reported the tap-to-expand wasn't discoverable -- nothing
about a `ReasoningRow` visually distinguished it from a plain, non-interactive one. Added a trailing
up/down chevron (`ic_arrow_up`/`ic_arrow_down`, `icon_size_small`, `onSurfaceMuted`) to the end of
the row, shown ONLY when `meaning != null` -- the same in-place-expand-toggle icon
`SynthesisExpandableHeroSample` and every other "reveal more of this same card" control in the app
already uses (see `compose-conventions.md`'s "See more" section, which explicitly separates this
icon family from the navigate-away chevron). The row's `clickable` modifier is now also
conditional on `meaning != null` -- a row with nothing to reveal is no longer tappable at all, so
the affordance never implies an action that does nothing. `SignalConditions.kt`'s `CategoryGroup`
has the exact same "tap with no visual hint" gap for `condition_labels`' own `meaning` field and
was NOT touched here (out of scope for this ask) -- flagged as a follow-up, not fixed silently.

## Sixteenth step (2026-09-07): `StockAnalysisGlossaryBottomSheet` matches the Regime/Bands card style too

`StockAnalysisGlossaryBottomSheet` (the whole-card multi-metric glossary sheet — Key Levels,
Fundamentals, Macro, Returns, HeadlineMetricsStrip, MomentumAndTrend, SetupReasoning all open it
from their own info icon) still had its pre-Twelfth-step look: a plain `Column` per entry,
`titleMedium`/`bodyMedium`. Brought in line with the same "list of terms" card shape
`MarketGlossaryBottomSheet`'s term cards were fixed to match a day earlier (Twelfth/Fifteenth
steps, itself matching Indicators' `BandRow`):

- Each entry is now its own `PulseCard(DATA)`, `titleSmall`/`bodySmall` (was plain
  `titleMedium`/`bodyMedium` with no card at all).
- **No accent border or "CURRENT" badge here** — deliberately not a full match to `BandRow`. This
  sheet lists every metric a section renders, not one reading against a set of bands, so there's no
  "current" concept to highlight; only the card shape and type scale carry over.
- Title dropped from `headlineSmall` to `titleLarge`, matching `MarketGlossaryBottomSheet`'s own
  title-size correction.
- **`containerColor` fixed proactively, same day, before it could reproduce the Thirteenth step's
  bug**: since entries now nest `PulseCard(DATA)`, `surfaceContainerHighest` would have resolved to
  the literal same value as the card's own fill (this app's simplified surface ramp, see Thirteenth
  step) and made every card invisible against the sheet. Set to `colorScheme.surface` up front
  instead of waiting for a bug report.

## Open items for the next pass (not yet decided — don't assume an answer)

1. **Content-heading weight** (Signal/Sentiment bold vs. the four list cards plain) — intentional
   split by card role, or should it be unified alongside size?
2. **Which-family-for-a-new-card rule** (section-title vs. eyebrow, above) is inferred from the
   current 12 cards, not confirmed by whoever owns the design system.
3. **`theming-spec.md` §8** still only describes the older DATA/SYNTHESIS title-tier table and
   doesn't mention either header family here — worth reconciling once these conventions are
   confirmed stable, so there's one authoritative typography section instead of two.
4. **Everything above "Resolved" is Summary-only; the rollout to other screens is tracked in
   "Resolved" as it happens** — as of 2026-09-06: Indicators' "Today's Read" and Insights' "Digest"
   (step 1, spacing/icon-sizing only), Insights' Positioning (step 2, group headers moved inside
   their cards), Stock Detail's Digest + the Deep Dive screen (step 3, converged onto
   `CardEyebrowLabel` — the app-wide "ai style card" family — not Stock Detail's own
   `SynthesisCardHeader`), and Indicators' Horizons cards (step 6, same `CardEyebrowLabel`
   convergence plus a spacing fix). News, and the DATA-style cards on Stocks/Indicators/Insights,
   are still untouched — don't treat a pattern documented here as already applied to a screen or
   card family not named in "Resolved."
5. **Stock Detail still runs two SYNTHESIS-card header families side by side, shrinking one card
   at a time**: `DeepStudy`/`Scenarios` remain on the screen's own `SynthesisCardHeader`
   (`titleMedium.Bold`); `TechnicalRead`/`DigestCard`/`DeepDiveCard`/`DeepDiveSectionCard` are now
   on `CardEyebrowLabel` (`labelSmall`, the Market Signal family and, per the norm section above,
   the standard for every SYNTHESIS card going forward). The owner is converting the remaining two
   individually rather than in one sweep — don't convert `DeepStudy`/`Scenarios` without being
   asked, and don't assume they're done just because the norm is written down.
