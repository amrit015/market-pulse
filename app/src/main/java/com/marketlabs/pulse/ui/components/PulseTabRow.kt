package com.marketlabs.pulse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
 * no way to see it was even selected. 2026-09-05 fix, surfaced once Stock Detail's tab content
 * became swipeable and its 6 tabs no longer all fit on-screen at once.
 *
 * `highlightedTabIndex` (optional, e.g. a screen's "Favorites" tab) gets a small filled-star glyph
 * before its label -- in both the selected and unselected state, tinted `accentOn` (matching the
 * label's own selected-state color, for contrast against the solid fill) or `accentPrimary`
 * (matching the label's own unselected-state role) respectively. No border change -- the star glyph
 * alone is enough to mark the chip without also weighing down its outline.
 *
 * Small `<`/`>` overflow chevrons fade in at either edge whenever this row's own horizontal scroll
 * has more content in that direction (`canScrollBackward`/`canScrollForward`, read off the same
 * `rememberScrollState()` the row scrolls with) -- purely a "there's more, keep scrolling" cue, not
 * a tap target of their own (the row itself is already the scrollable surface). Each sits on a small
 * circular `surfaceVariant` scrim (one step lighter than the page, same role `PulseCard`'s DATA style
 * uses) so it stays legible over whatever chip content has scrolled underneath it -- `background`
 * itself is what an unselected chip's own fill already uses, so that color wouldn't have stood apart.
 */
private const val TabSelectionAnimationMs = 200

@Composable
fun PulseTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    highlightedTabIndex: Int? = null
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

    LaunchedEffect(selectedTabIndex) {
        bringIntoViewRequesters.getOrNull(selectedTabIndex)?.bringIntoView()
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(
                    horizontal = dimensionResource(id = R.dimen.padding_large),
                    vertical = dimensionResource(id = R.dimen.padding_medium)
                ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            tabs.forEachIndexed { index, label ->
                val isSelected = index == selectedTabIndex
                val isHighlighted = index == highlightedTabIndex
                val fillColor by animateColorAsState(
                    targetValue = if (isSelected) pulseColors.accentPrimary else MaterialTheme.colorScheme.background,
                    animationSpec = tween(TabSelectionAnimationMs),
                    label = "tab_fill"
                )
                // 💡 Always a real border, animated between the fill color (selected -- blends away,
                // same look as the old `null`) and the outline color (unselected) -- `BorderStroke`
                // itself can't be cross-faded since it isn't a `Color`. Unaffected by `isHighlighted`
                // -- that star glyph alone is enough to stand out; an accent border on top of it read
                // as too heavy.
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) pulseColors.accentPrimary else pulseColors.accentSurfaceBorder,
                    animationSpec = tween(TabSelectionAnimationMs),
                    label = "tab_border"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) pulseColors.accentOn else pulseColors.onSurfaceMuted,
                    animationSpec = tween(TabSelectionAnimationMs),
                    label = "tab_text"
                )
                // 💡 The star stays visible in both states (was hidden the moment the tab became
                // selected) -- `accentOn` when selected, same contrast color the label text switches
                // to against the solid `accentPrimary` fill; `accentPrimary` otherwise, same as the
                // unselected label's own accent-on-muted-background treatment reversed.
                val starTint by animateColorAsState(
                    targetValue = if (isSelected) pulseColors.accentOn else pulseColors.accentPrimary,
                    animationSpec = tween(TabSelectionAnimationMs),
                    label = "tab_star"
                )
                Surface(
                    color = fillColor,
                    border = BorderStroke(dimensionResource(id = R.dimen.border_thin), borderColor),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small)),
                    modifier = Modifier
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

        if (canScrollBackward) {
            TabRowOverflowChevron(pointsLeft = true, modifier = Modifier.align(Alignment.CenterStart))
        }
        if (canScrollForward) {
            TabRowOverflowChevron(pointsLeft = false, modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
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
