package com.marketlabs.pulse.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * The one tab-bar design for every screen in this app that switches between a handful of sibling
 * sections -- a horizontally-scrolling row of segmented-control-style chips: a solid `accentPrimary`
 * fill (`accentOn` text) for the selected chip, an outlined `accentSurfaceBorder` hairline border
 * (`onSurfaceMuted` text) for the rest, `corner_radius_small` shape. `horizontalScroll` rather than
 * a fixed-width `Row` since chip width follows each label's own content instead of splitting the
 * row into N equal columns -- appropriate for tab labels that vary a lot in length (e.g.
 * "Technicals" vs "News", or "Positioning" vs "Risks").
 *
 * Established on the Stock Analysis detail screen (was `StockDetailRoute`'s private
 * `DetailPillTabRow`) and pulled out here once Insights needed the identical pattern, so every
 * future screen with page-level tabs reaches for this instead of hand-rolling a third
 * near-duplicate. `ChartRangePicker` shares this same visual language (fill/outline treatment,
 * `corner_radius_small`, `labelMedium` bold) but deliberately stays its own component -- it's an
 * evenly-weighted range selector (`Modifier.weight(1f)` per button, no scrolling), a different
 * layout shape than tabs that size to their own label content, not a page-tab bar itself.
 *
 * Pattern for callers (see `StockDetailViewModel`/`StockDetailScreen` or
 * `InsightsViewModel`/`InsightsScreen` for full worked examples): define a per-screen `enum class
 * XTab(val labelRes: Int)`, keep the selected index as a `MutableStateFlow<Int>` in the ViewModel
 * with an `onTabSelected(index: Int)` setter, pass `XTab.entries.map { stringResource(it.labelRes) }`
 * as `tabs` here, and branch the screen's content on `XTab.entries[selectedTabIndex]`.
 *
 * Each chip carries its own [BringIntoViewRequester], and selecting a tab -- whether by tapping a
 * chip here or (on a screen with swipeable tab content) settling a swipe several tabs away --
 * scrolls this row just enough to bring that chip on-screen. Without this, a tab index changing
 * from outside a tap on this row (a pager swipe landing on a tab this row hasn't scrolled to) left
 * the row wherever it last was, silently stranding whichever tab sits at either end off-screen with
 * no way to see it was even selected. That happens once tab content is swipeable and the tabs no
 * longer all fit on-screen at once (e.g. Stock Detail's 6 tabs).
 *
 * `highlightedTabIndex` (optional, e.g. a screen's "Favorites" tab) gets a small filled-star glyph
 * before its label -- in both the selected and unselected state, tinted `accentOn` (matching the
 * label's own selected-state color, for contrast against the solid fill) or `accentPrimary`
 * (matching the label's own unselected-state role) respectively. No border change -- the star glyph
 * alone is enough to mark the chip without also weighing down its outline.
 *
 * Small `<`/`>` overflow chevrons appear in their own slots at the row's start/end whenever this
 * row's horizontal scroll has more content in that direction (`canScrollBackward`/`canScrollForward`,
 * read off the same `rememberScrollState()` the row scrolls with) -- purely a "there's more, keep
 * scrolling" cue, not a tap target of their own (the row itself is already the scrollable surface).
 * Each sits in its own `Box` outside the scrollable chip content, not overlaid on top of it, so a
 * chevron can never cover part of whichever chip has scrolled to that edge -- and that slot animates
 * its width to zero when its direction has nothing left to scroll, so the tab chips get that width
 * back instead of a permanent empty gutter sitting unused at one end. Selecting a tab also brings it
 * into view padded by [R.dimen.tab_row_scroll_peek_margin] on both sides (not just its own bounds),
 * so the tab just past the newly-selected one is already peeking into view rather than only
 * appearing once it's selected itself.
 */
private const val TabSelectionAnimationMs = 250

/**
 * The selected chip's fill is one shared highlight that slides between chips rather than each chip
 * fading its own fill in and out: [selectionPosition] is the (fractional) index it sits at -- a
 * screen with a swipeable pager passes `pagerState.currentPage + pagerState.currentPageOffsetFraction`
 * so the highlight (and the chips' text/border colors) track the swipe finger-for-finger instead of
 * waiting for the pager to settle; a screen without a pager leaves it `null` and the highlight
 * animates to [selectedTabIndex] on its own, so a tap slides it too. [selectedTabIndex] still drives
 * everything else (which chip scrolls into view, what a tap reports).
 */
@Composable
fun PulseTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    highlightedTabIndex: Int? = null,
    selectionPosition: (() -> Float)? = null
) {
    val pulseColors = LocalPulseColors.current
    val bringIntoViewRequesters = remember(tabs.size) { List(tabs.size) { BringIntoViewRequester() } }
    val scrollState = rememberScrollState()
    // 💡 `derivedStateOf`, not a direct `scrollState.value`/`maxValue` read below -- `ScrollState`'s
    // `value` is `@FrequentlyChangingValue` (updates every scrolled pixel), so reading it straight
    // in composition would recompose this whole row on every frame of a drag. These two booleans
    // only actually change when the row crosses into/out of "more content this direction," so
    // deriving them confines the frequent reads to this lambda and only recomposes the chevrons'
    // `if` branches on an actual edge-state flip.
    val canScrollBackward by remember { derivedStateOf { scrollState.value > 0 } }
    val canScrollForward by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }

    // Each chip's bounds inside the chip row, measured after layout -- the sliding highlight needs
    // them to know where to sit and how wide to be.
    val chipBounds = remember(tabs.size) { mutableStateMapOf<Int, Rect>() }

    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scrollPeekMarginPx = with(density) { dimensionResource(id = R.dimen.tab_row_scroll_peek_margin).toPx() }
    // Chip bounds aren't known until after the first layout pass, so this can't just seed itself
    // from `selectedTabIndex` -- that would fire a haptic tick on first composition (any screen
    // that opens with, say, its 3rd tab already selected).
    var previousSelectedTabIndex by remember { mutableIntStateOf(selectedTabIndex) }
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex != previousSelectedTabIndex) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        previousSelectedTabIndex = selectedTabIndex
        // Bring the selected chip into view padded by a peek margin on both sides, not just its own
        // bounds -- so landing on (or swiping to) the second-to-last tab already reveals a sliver of
        // the last one, rather than only appearing once that tab is itself selected.
        val bounds = chipBounds[selectedTabIndex]
        val request = bounds?.let { Rect(-scrollPeekMarginPx, 0f, it.width + scrollPeekMarginPx, it.height) }
        bringIntoViewRequesters.getOrNull(selectedTabIndex)?.bringIntoView(request)
    }

    val animatedPosition by animateFloatAsState(
        targetValue = selectedTabIndex.toFloat(),
        animationSpec = tween(TabSelectionAnimationMs),
        label = "tab_selection_position"
    )
    val position = { (selectionPosition?.invoke() ?: animatedPosition).coerceIn(0f, (tabs.size - 1).coerceAtLeast(0).toFloat()) }
    val boundsReady = chipBounds.size == tabs.size
    val shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small))
    val chevronSlotSize = dimensionResource(id = R.dimen.icon_size_extra_large)
    // Collapses to zero rather than staying reserved when there's nothing to scroll that
    // direction -- the tab chips get that width back instead of leaving a permanent empty gutter.
    val startSlotWidth by animateDpAsState(
        targetValue = if (canScrollBackward) chevronSlotSize else 0.dp,
        label = "tab_row_start_chevron_width"
    )
    val endSlotWidth by animateDpAsState(
        targetValue = if (canScrollForward) chevronSlotSize else 0.dp,
        label = "tab_row_end_chevron_width"
    )

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // Slots at the row's true start/end, never overlapping tab content -- a chevron appearing
        // here can't cover whichever chip happens to have scrolled to that edge, the way an
        // absolutely-positioned overlay on top of the scrollable content would.
        Box(
            modifier = Modifier.size(width = startSlotWidth, height = chevronSlotSize),
            contentAlignment = Alignment.Center
        ) {
            if (canScrollBackward) TabRowOverflowChevron(pointsLeft = true)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState)
                .padding(
                    horizontal = dimensionResource(id = R.dimen.padding_large),
                    vertical = dimensionResource(id = R.dimen.padding_medium)
                )
        ) {
            if (boundsReady) {
                // Drawn behind the chips (which are transparent) so their text sits on top of it.
                Box(
                    modifier = Modifier
                        .offset {
                            val (left, _) = interpolatedBounds(chipBounds, position())
                            IntOffset(left.roundToInt(), chipBounds.getValue(0).top.roundToInt())
                        }
                        .layout { measurable, constraints ->
                            val (_, width) = interpolatedBounds(chipBounds, position())
                            val height = chipBounds.getValue(0).height.roundToInt()
                            val placeable = measurable.measure(Constraints.fixed(width.roundToInt().coerceAtLeast(0), height))
                            layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                        }
                        .clip(shape)
                        .background(pulseColors.accentPrimary)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {
                tabs.forEachIndexed { index, label ->
                    val isHighlighted = index == highlightedTabIndex
                    // How selected this chip looks right now: 1 when the highlight sits exactly on it,
                    // fading to 0 one chip away -- read inside `graphicsLayer`-free color lerps below so
                    // it tracks the position frame by frame.
                    val selectedness = (1f - abs(position() - index)).coerceIn(0f, 1f)
                    val textColor = lerp(pulseColors.onSurfaceMuted, pulseColors.accentOn, selectedness)
                    val borderColor = lerp(pulseColors.accentSurfaceBorder, pulseColors.accentPrimary, selectedness)
                    val starTint = lerp(pulseColors.accentPrimary, pulseColors.accentOn, selectedness)
                    Surface(
                        // Transparent once the highlight can render; until the chips have been measured
                        // (first frame only) the selected chip fills itself so it's never unreadable.
                        color = if (!boundsReady && index == selectedTabIndex) pulseColors.accentPrimary else Color.Transparent,
                        border = BorderStroke(dimensionResource(id = R.dimen.border_thin), borderColor),
                        shape = shape,
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInParent()
                                val bounds = Rect(position.x, position.y, position.x + coordinates.size.width, position.y + coordinates.size.height)
                                if (chipBounds[index] != bounds) chipBounds[index] = bounds
                            }
                            .bringIntoViewRequester(bringIntoViewRequesters[index])
                            .clickable { onTabSelected(index) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(
                                horizontal = dimensionResource(id = R.dimen.padding_large),
                                vertical = dimensionResource(id = R.dimen.padding_medium)
                            )
                        ) {
                            if (isHighlighted) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_star_filled),
                                    contentDescription = null,
                                    tint = starTint,
                                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                                )
                                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_tiny)))
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier.size(width = endSlotWidth, height = chevronSlotSize),
            contentAlignment = Alignment.Center
        ) {
            if (canScrollForward) TabRowOverflowChevron(pointsLeft = false)
        }
    }
}

/** Left edge and width of the highlight at fractional index [position], blended between the two chips it sits between. */
private fun interpolatedBounds(chipBounds: Map<Int, Rect>, position: Float): Pair<Float, Float> {
    val lower = position.toInt().coerceIn(0, chipBounds.size - 1)
    val upper = (lower + 1).coerceAtMost(chipBounds.size - 1)
    val fraction = (position - lower).coerceIn(0f, 1f)
    val from = chipBounds.getValue(lower)
    val to = chipBounds.getValue(upper)
    return (from.left + (to.left - from.left) * fraction) to (from.width + (to.width - from.width) * fraction)
}

@Composable
private fun TabRowOverflowChevron(pointsLeft: Boolean, modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        // 💡 `surfaceVariant`, not `colorScheme.background` -- background is what an unselected
        // chip's own fill already uses (`PulseTabRow`'s `fillColor` above), so a same-color scrim
        // blended straight into whatever chip had scrolled underneath it instead of standing apart
        // from it. `surfaceVariant` is the same "one step lighter than the page" surface `PulseCard`'s
        // DATA style already uses for exactly this kind of visible-against-the-page separation.
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.size(dimensionResource(id = R.dimen.icon_size_extra_large))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_forward),
                contentDescription = null,
                tint = LocalPulseColors.current.onSurfaceMuted,
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.icon_size_small))
                    .graphicsLayer { rotationZ = if (pointsLeft) 180f else 0f }
            )
        }
    }
}

/**
 * One [LazyListState] per tab of a `PulseTabRow` + `HorizontalPager` screen, `rememberSaveable`
 * across the whole list -- not a plain `remember`, which only survives `HorizontalPager` disposing
 * an off-screen page (a fresh `rememberLazyListState()` scoped to each page's own composition would
 * already handle that case). This screen-level list also has to survive the screen's own
 * composition being torn down and recreated -- e.g. navigating to a tapped item's detail screen and
 * back -- which a plain `remember` does not: `rememberSaveable` is what actually round-trips through
 * that, since it's backed by the destination's own `NavBackStackEntry`-scoped save/restore, not by
 * whatever happens to still be in memory.
 */
@Composable
fun rememberPerTabLazyListStates(tabCount: Int): List<LazyListState> = rememberSaveable(saver = perTabLazyListStatesSaver) {
    List(tabCount) { LazyListState() }
}

// 💡 Saves just `firstVisibleItemIndex`/`firstVisibleItemScrollOffset` per state via `LazyListState`'s
// own public constructor, rather than `LazyListState.Saver` -- that Saver's declared type is star-
// projected (`Saver<LazyListState, *>`), which the compiler won't let a caller outside its own
// declaration call `restore` on.
private val perTabLazyListStatesSaver: Saver<List<LazyListState>, Any> = listSaver(
    save = { states -> states.flatMap { listOf(it.firstVisibleItemIndex, it.firstVisibleItemScrollOffset) } },
    restore = { saved -> saved.chunked(2).map { (index, offset) -> LazyListState(index, offset) } }
)

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewPulseTabRowLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PulseTabRow(tabs = listOf("Playbook", "Risks", "Posture", "Positioning"), selectedTabIndex = 0, onTabSelected = {})
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewPulseTabRowDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PulseTabRow(tabs = listOf("Technicals", "Fundamentals", "Thesis", "Timeline", "News"), selectedTabIndex = 2, onTabSelected = {})
    }
}

@Preview(name = "Favorites highlighted", showBackground = true)
@Composable
private fun PreviewPulseTabRowFavorites() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PulseTabRow(
            tabs = listOf("Favorites", "Stocks", "Indices/ETF"),
            selectedTabIndex = 1,
            onTabSelected = {},
            highlightedTabIndex = 0
        )
    }
}

@Preview(name = "Overflow chevrons (narrow)", showBackground = true, widthDp = 220)
@Composable
private fun PreviewPulseTabRowOverflow() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PulseTabRow(
            tabs = listOf("Favorites", "Momentum", "Systemic Risk", "Valuation", "Macro Vitals"),
            selectedTabIndex = 2,
            onTabSelected = {},
            highlightedTabIndex = 0
        )
    }
}
