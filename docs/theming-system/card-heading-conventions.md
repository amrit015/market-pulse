# Card heading conventions — pilot on Summary, not yet repo-wide

> **Status: LIVING / IN PROGRESS.** This documents a set of card-heading/spacing conventions
> established on the Summary screen (`ui/screens/summary/views/SummaryScreen.kt`) during a
> 2026-09-05 pass, with the explicit intent to roll the same conventions out to other screens
> (Stocks, Indicators, Insights, News, ...) once they're settled. Nothing here has been applied
> outside Summary yet. Several open questions are called out explicitly at the bottom rather than
> silently resolved — read those before extending this pattern to a new screen. Verified against
> source on 2026-09-05; re-check line numbers before citing them if this file is read much later.

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

## Open items for the next pass (not yet decided — don't assume an answer)

1. **Content-heading weight** (Signal/Sentiment bold vs. the four list cards plain) — intentional
   split by card role, or should it be unified alongside size?
2. **Which-family-for-a-new-card rule** (section-title vs. eyebrow, above) is inferred from the
   current 12 cards, not confirmed by whoever owns the design system.
3. **`theming-spec.md` §8** still only describes the older DATA/SYNTHESIS title-tier table and
   doesn't mention either header family here — worth reconciling once these conventions are
   confirmed stable, so there's one authoritative typography section instead of two.
4. **Everything here is Summary-only.** Extending it to Stocks/Indicators/Insights/News is the
   explicit next step, not yet started — don't treat a pattern documented here as already applied
   elsewhere just because it's written down.
