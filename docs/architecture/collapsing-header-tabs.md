# Collapsing header + sticky tabs + swipeable pager

> **Status: LIVING.** This documents a scroll/layout pattern built 2026-09-05/06 for Stock Detail
> and Indicators, through several rounds of real bugs and corrections -- read the "Bugs hit and
> fixes" section before copying this pattern to a new screen; more than one thing here looks
> reasonable and isn't. This is a companion to `@docs/theming-system/card-heading-conventions.md`
> (which covers card/heading/spacing rules) and `@docs/guidelines/compose-conventions.md`'s
> `PulseTabRow` section (which covers the base tab-bar pattern every tabbed screen uses, with or
> without this heavier mechanism) -- this file is specifically about the collapsing-chrome
> mechanics, not styling. Verified against source on 2026-09-06; Summary's simpler pinned-zone
> variant (added 2026-09-07, by a separate session) folded in without a fresh full re-verification
> of the rest of this file -- re-check line numbers/specifics before citing them if read much later.

## What this pattern is for

A screen where some chrome above a `PulseTabRow` (a banner, an AI summary card, a nav card) should
behave like a normal scrollable item -- visible up front, scrolls away as the reader scrolls down
into tab content, reappears on scrolling back up -- while the tab row itself becomes sticky (parks
at a fixed position) once that chrome has scrolled fully out of view. Not every tabbed screen needs
this: **Insights doesn't** -- its `PulseTabRow` is simply pinned, unconditionally, with nothing
above it that needs to collapse. Reach for this only when a screen actually has scrollable chrome
above its tabs; a plain pinned tab row (see `compose-conventions.md`) is the default.

Two live examples of the FULL pattern (pinned zone + collapsing chrome + `NestedScrollConnection`),
different in how much chrome is pinned vs. collapsing:

- **Stock Detail** (`StockDetailRoute.kt`): `DetailHeader` (ticker/price) is the only thing
  genuinely pinned. The Deep Dive banner + `TechnicalRead` + the HIGH-urgency alert are all one
  collapsing region; the tab row settles directly below `DetailHeader` once that's scrolled away.
- **Indicators** (`IndicatorsScreen.kt`'s `IndicatorsMainFeed`): `AnalyzedAtHeader` (the timestamp)
  is the only thing pinned. Today's Read (the AI executive briefing card, itself expandable in
  place) + the Horizons nav card are the collapsing region; the tab row settles below the
  timestamp.

**Summary** (`SummaryScreen.kt`'s `MarketSummaryScreen`, added 2026-09-07) is a THIRD case, but the
simpler shape Insights already established, not the full pattern above -- worth naming explicitly
so a reader doesn't assume it needs pieces #2/#3 below just because it's listed here. Its pinned
zone (`SummaryCalendarStrip`, the 7-day date pills) never collapses at all -- there's no
collapsing-chrome `Box`/`Modifier.layout` override and no `NestedScrollConnection`, because nothing
above the pager needs to scroll away. What it DOES reuse: the `weight(1f)` `Box` + `HorizontalPager`
shape (piece #4) and the full "Pager ↔ ViewModel sync" sub-pattern below (a swipe between calendar
days is exactly a tab swipe, just keyed by `dateId` instead of a tab index) -- and, per Bug #4
below, the same `MainActivity.kt` global-top-bar fix Indicators/Insights needed, for the identical
reason.

## The four pieces

1. **A pinned zone** -- ordinary, non-scrolling Compose content sitting above everything else.
   Can be empty (nothing pinned) if the whole chrome should collapse.
2. **A collapsing chrome region** -- one `Box` whose height is `(chromeHeightPx +
   collapseOffsetPx).coerceIn(0f, chromeHeightPx)`, `clipToBounds()`, wrapping a `Column` with a
   `Modifier.layout { measurable, constraints -> ... }` override that:
   - Measures its child with `constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)`
     -- **ignoring** whatever height the shrinking Box above it is currently passing down. This is
     the one non-obvious part: without it, `chromeHeightPx` starts at `0f`, so the Box's height on
     the very first frame is `0f`, which would squeeze the child to zero height too, and it could
     never report its true natural height back to break out of that. Measuring unbounded sidesteps
     the chicken-and-egg entirely.
   - Writes the measured height into a `mutableFloatStateOf` (`chromeHeightPx`) if it changed.
   - Reports `layout(placeable.width, 0)` -- **zero height** as this node's own contribution to the
     parent Box's sizing. The Box's height is driven entirely by the `chromeHeightPx`/
     `collapseOffsetPx` state, not by this child, so this must not also feed into it.
   - Places the child at `y = collapseOffsetPx.roundToInt()` (0 when fully expanded, negative as
     it collapses).
3. **A `NestedScrollConnection`** (`chromeNestedScrollConnection` / `bannerNestedScrollConnection`
   in the two live examples) with two halves:
   - `onPreScroll`: claims upward drags (`available.y < 0`) *before* any list below gets to scroll,
     shrinking `collapseOffsetPx` toward `-chromeHeightPx`. This is what makes the chrome finish
     collapsing before the tab content underneath it moves at all.
   - `onPostScroll`: claims downward drags (`available.y > 0`) that a list below **couldn't**
     consume itself (i.e. it's already at its own top), expanding `collapseOffsetPx` back toward
     `0f`. This is what lets scrolling down at a tab's own top reopen the chrome.
   - Both clamp with `.coerceIn(-chromeHeightPx, 0f)` and return only the delta actually consumed,
     letting the rest continue for whatever normally handles it.
4. **`PulseTabRow`**, then a **`weight(1f)` `Box`** wrapping the `HorizontalPager`, with the
   `NestedScrollConnection` from #3 attached via `Modifier.nestedScroll(...)` on that same Box.

The tab row needs **no special positioning code at all** -- because the chrome Box's own height
shrinks as `collapseOffsetPx` goes negative, `PulseTabRow` (an ordinary sibling right after it in
the enclosing `Column`) naturally rides up and parks itself the moment the chrome Box reaches 0dp.
"Sticky" here is just "the thing above it can't get any shorter."

## Where the `NestedScrollConnection` goes relative to `PullToRefreshBox`

Attach it to a `Box` **inside** `PullToRefreshBox`'s content lambda (wrapping the `HorizontalPager`
directly), not on an ancestor Box wrapping `PullToRefreshBox` itself. Compose's nested-scroll
dispatch runs nearest-connection-first in both directions (`onPreScroll` bubbles up starting at the
nearest ancestor; `onPostScroll` does the same with the leftover). Putting this connection nearer
to the pager than `PullToRefreshBox`'s own internal connection means:

- **Collapsing** (upward drag): this connection's `onPreScroll` fires before `PullToRefreshBox`'s
  own, which doesn't care about upward drags anyway -- no conflict either way.
- **Expanding** (downward drag at a list's top): this connection's `onPostScroll` gets first
  refusal on the leftover, so the chrome finishes re-expanding *before* `PullToRefreshBox` ever
  sees anything to turn into a pull-to-refresh gesture. Get this backwards (attach outside/above
  `PullToRefreshBox`) and pull-to-refresh eagerly claims every bit of downward overscroll at the
  top first, and the chrome can never reopen via scroll at all.

## Pager ↔ ViewModel sync

Exactly the same two-effect shape `InsightsRoute.kt` established first, reused verbatim by
`StockDetailRoute.kt`, `IndicatorsScreen.kt`, and `SummaryScreen.kt` (keyed by `dateId` instead of
a tab index -- see that file's own doc comment on `MarketSummaryScreen`):

```kotlin
val pagerState = rememberPagerState(initialPage = uiState.selectedTabIndex) { XTab.entries.size }

LaunchedEffect(uiState.selectedTabIndex) {
    if (pagerState.currentPage != uiState.selectedTabIndex) {
        pagerState.animateScrollToPage(uiState.selectedTabIndex)
    }
}

// `settledPage`, NOT `currentPage` -- see next section.
LaunchedEffect(pagerState.settledPage) {
    if (pagerState.settledPage != uiState.selectedTabIndex) {
        viewModel.onTabSelected(pagerState.settledPage)
    }
}
```

Tap a `PulseTabRow` chip → `selectedTabIndex` changes → first effect animates the pager to match.
Swipe the pager → `settledPage` changes once the swipe/fling finishes → second effect tells the
ViewModel, which the first effect then sees as already-matching (no-op, no fight). Both are
one-directional and only act when the two are out of sync, so neither ever chases the other.

## Bugs hit and fixes (read before reusing this pattern)

1. **`currentPage` vs `settledPage`.** `currentPage` updates continuously the moment scroll
   progress crosses the halfway point between two pages -- including mid-*animation* from the tap
   effect above, or mid-fling from a fast multi-page swipe. Keying the swipe→ViewModel effect off
   `currentPage` pushes an intermediate, not-yet-final page value to the ViewModel, which the other
   effect then reads and uses to correct the pager -- fighting whatever gesture/animation is still
   in flight and leaving the pager stuck between two pages. `settledPage` only updates once the
   pager has actually come to rest.
2. **`PulseTabRow` not scrolling to reveal a swipe-selected tab.** The tab row's own horizontal
   `ScrollState` never reacted to `selectedTabIndex` changing -- invisible before this pattern
   existed, because a tab could previously only be selected by tapping a chip that was, by
   definition, already on-screen. Once a pager swipe could jump `selectedTabIndex` to a tab several
   positions away, the row stayed wherever it was and stranded the first/last tabs off-screen with
   no way to reach them. Fixed in the shared component itself (`PulseTabRow.kt`): each chip carries
   a `BringIntoViewRequester`, and a `LaunchedEffect(selectedTabIndex)` calls `bringIntoView()` on
   the newly-selected chip, whether it got there by tap or swipe.
3. **A `weight(1f)`-less pager Box can go completely off-screen and unreachable.** The pager's
   wrapping `Box` was originally just `Modifier.fillMaxSize()`, with no `weight(1f)`, inside the
   outer `Column`. If the chrome above it (pinned zone + collapsing region combined) ever measures
   taller than the remaining screen height -- e.g. Indicators' Today's Read expanding in place to
   show `what_changed` + `shifts[]`, with no cap of its own -- a plain `Column` does **not** shrink
   earlier children to fit; it lets everything overflow, pushing the pager (and the only
   scrollable, touchable content in it) off the bottom of the screen with **no way to scroll back
   to it** -- a real, reproduced, total scroll lockup. `Modifier.weight(1f)` on that Box guarantees
   it always gets whatever space is actually left, even a thin sliver -- which is still enough to
   catch a scroll gesture and collapse the chrome back down via the `NestedScrollConnection`,
   restoring the rest of the space. Applied to both `StockDetailRoute.kt` and
   `IndicatorsScreen.kt`; treat it as required on every future screen using this pattern, not just
   the ones that happen to have an expandable card in their chrome -- long-enough static content on
   a small-enough screen can trigger the identical overflow.
   - **The wrong fix that was tried first:** isolating the expandable card (Today's Read) into its
     own pinned zone with its own internal `heightIn(max = ...).verticalScroll(...)` cap on its
     expanded detail, so it could never grow past a fixed height. This "solved" the immediate
     symptom but was explicitly rejected: it made that one card scroll independently of the page,
     which reads wrong (a reader expects the whole page to scroll, not to hit a second, nested
     scroll surface inside one card). The correct fix is `weight(1f)` on the pager -- a general
     safety net that doesn't require guessing which chrome element might grow unboundedly, and
     doesn't change how any individual card behaves. An expandable card's content should show its
     full natural height when expanded and rely on the page-level scroll (plus this safety net) to
     reach whatever that pushes further down -- the same way a tall item in any list is reached by
     scrolling the list, not by scrolling inside the item.
4. **The global top app bar fighting a screen's own collapsing chrome.** `MainActivity.kt` used
   one shared `TopAppBarDefaults.enterAlwaysScrollBehavior()` for every route, attached via
   `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)` on the outer `Scaffold` --
   meaning it sits as an ancestor of every screen's content, including this pattern's own
   `NestedScrollConnection` further down the tree. Two independent connections competing over the
   same scroll-delta stream meant the in-screen chrome consumed part of each drag before the global
   bar's connection (further up, seeing less and less of it) ever got a consistent read on it,
   making the bar's hide/show animation behave erratically. Fixed by giving routes that use this
   pattern (`MARKET_INDICATORS`, `MARKET_INSIGHTS`) a `TopAppBarDefaults.pinnedScrollBehavior()`
   instead -- it simply never reacts to scroll, so the global bar is fully static there and leaves
   the entire scroll-delta stream for the screen's own chrome to consume. Every other route keeps
   the original collapsing behavior. If a future screen adopts this pattern while still using the
   global top bar (rather than suppressing it entirely, `isPushedDestination`-style, the way Stock
   Detail does), it needs the same pinned-behavior treatment in `MainActivity.kt` or it will
   reproduce this exact conflict.
   - **Materialized exactly as predicted, 2026-09-07:** `MARKET_SUMMARY` adopted the pager shape
     above (see "Summary" under "What this pattern is for") and hit this identical conflict even
     though it has no collapsing chrome at all -- the pinned calendar strip alone was enough to
     fight the global bar the same way. Added to the same `hasStaticTopBar`/`pinnedScrollBehavior`
     list in `MainActivity.kt` as `MARKET_INDICATORS`/`MARKET_INSIGHTS`. Confirms this bug isn't
     specific to the full collapsing-chrome mechanism -- any screen with its OWN pinned zone above a
     `HorizontalPager`, collapsing or not, needs this treatment.

## Checklist for adding this to a new screen

1. Does it actually need this? If nothing above the tab row needs to scroll away, just pin
   `PulseTabRow` directly (see `compose-conventions.md`) -- don't reach for this pattern by default.
2. Decide what's pinned vs. collapsing. Anything with its own in-place expand/collapse (like
   Today's Read) is fine to put in the collapsing region -- don't isolate it into its own capped,
   independently-scrolling zone (bug #3's rejected fix).
3. Build the four pieces above; copy the exact `Modifier.layout` shape, don't improvise a
   different height-tracking mechanism.
4. Attach the `NestedScrollConnection` **inside** `PullToRefreshBox`'s content, wrapping the pager
   -- not around `PullToRefreshBox` itself.
5. Give the pager's wrapping `Box` `Modifier.weight(1f)` inside the outer `Column`. Always, not
   only when something in the chrome is known to expand.
6. Wire the pager↔ViewModel sync exactly as shown above, `settledPage` and all.
7. If the screen still shows the global top bar (doesn't suppress it via `isPushedDestination`),
   add its route to `MainActivity.kt`'s `hasStaticTopBar` check.
8. `PulseTabRow`'s own bring-into-view fix is already in the shared component -- nothing to do
   here, just don't reintroduce a per-screen tab bar that lacks it.
