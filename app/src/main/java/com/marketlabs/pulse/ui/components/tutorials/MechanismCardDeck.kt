package com.marketlabs.pulse.ui.components.tutorials

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp as lerpFloat
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.FormattedBodyText
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.PulseTabRow
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Horizontally swipeable cards with a filled circular arrow on whichever side(s) have another card
 * (none on the side you're already at the end of) and progress dots underneath. All cards share one
 * height -- the tallest card's, capped at [maxCardHeight] (a card scrolls past that) -- so the
 * caller decides how much of the screen the carousel may use, not this component. Used for the
 * per-screen "?" guide and for the Tutorials mechanism decks. [enlargedText] steps title/body up
 * one type-scale rung; the "?" guide leaves it off so its sheet stays compact, while the Tutorials
 * hub's full-screen carousels turn it on.
 */
@Composable
fun CardCarousel(
    pages: List<DeckPage>,
    maxCardHeight: Dp,
    modifier: Modifier = Modifier,
    enlargedText: Boolean = false,
    onNavigateToRoute: (String) -> Unit = {}
) {
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val arrowInset = dimensionResource(id = R.dimen.padding_small)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (pages.size > 1) {
            PulseTabRow(
                tabs = pages.map { it.tabLabel },
                selectedTabIndex = currentPage,
                onTabSelected = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
                selectionPosition = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_extra_large))
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            EqualHeightPager(
                pages = pages,
                pagerState = pagerState,
                maxCardHeight = maxCardHeight,
                enlargedText = enlargedText,
                onNavigateToRoute = onNavigateToRoute
            )
            if (currentPage > 0) {
                CarouselArrow(
                    pointsForward = false,
                    onClick = { scope.launch { pagerState.animateScrollToPage(currentPage - 1) } },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = arrowInset)
                )
            }
            if (currentPage < pages.lastIndex) {
                CarouselArrow(
                    pointsForward = true,
                    onClick = { scope.launch { pagerState.animateScrollToPage(currentPage + 1) } },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = arrowInset)
                )
            }
        }
        if (pages.size > 1) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            CarouselDots(pageCount = pages.size, position = pagerState.currentPage + pagerState.currentPageOffsetFraction)
        }
    }
}

/**
 * Every card is exactly as tall as the tallest card's text (capped at [maxCardHeight], where a card
 * scrolls instead), so swiping never changes the height. The tallest is found by measuring every
 * card once, off-screen, at the width a real card will get.
 */
@Composable
private fun EqualHeightPager(
    pages: List<DeckPage>,
    pagerState: PagerState,
    maxCardHeight: Dp,
    enlargedText: Boolean,
    onNavigateToRoute: (String) -> Unit
) {
    val sidePadding = dimensionResource(id = R.dimen.padding_extra_large)
    val pageSpacing = dimensionResource(id = R.dimen.padding_medium)

    SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
        val cardWidth = (constraints.maxWidth - sidePadding.roundToPx() * 2).coerceAtLeast(0)
        val probeConstraints = Constraints(minWidth = cardWidth, maxWidth = cardWidth)
        val tallest = subcompose("probe") {
            pages.forEach { page -> CarouselCard(page = page, fillHeight = false, enlargedText = enlargedText, onNavigateToRoute = onNavigateToRoute) }
        }.maxOfOrNull { it.measure(probeConstraints).height } ?: 0
        val pagerHeight = tallest.coerceAtMost(maxCardHeight.roundToPx())

        val pager = subcompose("pager") {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pagerHeight.toDp()),
                contentPadding = PaddingValues(horizontal = sidePadding),
                pageSpacing = pageSpacing
            ) { index ->
                CarouselCard(
                    page = pages[index],
                    fillHeight = true,
                    enlargedText = enlargedText,
                    onNavigateToRoute = onNavigateToRoute,
                    // Signed distance of this page from center, in pages -- same convention the
                    // onboarding carousel's own slide transition uses, passed as a lambda so it's
                    // only read inside `graphicsLayer`, which redraws every drag frame with no
                    // recomposition.
                    pageOffset = { ((pagerState.currentPage - index) + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f) }
                )
            }
        }.first().measure(constraints)

        layout(pager.width, pager.height) { pager.place(0, 0) }
    }
}

// A card one full page away from center is drawn at this scale/alpha; in between, both follow the
// drag linearly -- same magnitude as the onboarding carousel's own slide transition, so swiping
// between Learn cards reads as the same gesture.
private const val CardMinScale = 0.9f
private const val CardMinAlpha = 0.5f

@Composable
private fun CarouselCard(
    page: DeckPage,
    fillHeight: Boolean,
    enlargedText: Boolean = false,
    onNavigateToRoute: (String) -> Unit = {},
    pageOffset: () -> Float = { 0f }
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val scrollState = rememberScrollState()
    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (fillHeight) Modifier.fillMaxHeight() else Modifier)
            .graphicsLayer {
                val distance = abs(pageOffset())
                val scale = lerpFloat(1f, CardMinScale, distance)
                scaleX = scale
                scaleY = scale
                alpha = lerpFloat(1f, CardMinAlpha, distance)
            }
    ) {
        // Content sits vertically centered in the (equal-height) card; a card whose text is
        // taller than the space just fills it and scrolls.
        Box(
            modifier = if (fillHeight) Modifier.fillMaxSize() else Modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    // 💡 Only the real (fixed-height) cards scroll. The off-screen height probe is
                    // measured with an unbounded max height, and `verticalScroll` throws when it's
                    // measured that way -- so the probe must not carry it.
                    .then(if (fillHeight) Modifier.verticalScroll(scrollState) else Modifier)
                    // 💡 Wider horizontal inset than vertical: the circular arrows overlap the card's
                    // left/right edges by about half their width, so the text has to start clear of them.
                    // 💡 The tallest card's own height sets every card's height, so its content sits
                    // flush against this padding with no extra centering slack the way shorter cards
                    // get -- enlargedText's carousels (whose bigger type more often produces the
                    // tallest card) get a bit more of it than the "?" guide's compact one.
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.padding_extra_large),
                        vertical = if (enlargedText) dimensionResource(id = R.dimen.padding_xlarge) else paddingLarge
                    )
            ) {
                Text(
                    text = page.title,
                    style = (if (enlargedText) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall)
                        .copy(fontWeight = FontWeight.Bold),
                    color = LocalPulseColors.current.accentPrimary
                )
                FormattedBodyText(
                    text = page.body,
                    style = if (enlargedText) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
                )
                page.diagram?.let { diagram ->
                    Spacer(modifier = Modifier.height(paddingLarge))
                    diagram()
                }
                page.routes?.takeIf { it.isNotEmpty() }?.let { routes ->
                    Spacer(modifier = Modifier.height(paddingLarge))
                    LearnRouteChips(routes = routes, onNavigateToRoute = onNavigateToRoute)
                }
                page.trailingContent?.let { trailingContent ->
                    Spacer(modifier = Modifier.height(paddingLarge))
                    trailingContent()
                }
            }
            if (fillHeight) {
                ScrollFadeEdges(scrollState = scrollState, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

/**
 * A thin top/bottom gradient fading to the card's own background color, shown only on whichever
 * edge(s) [scrollState] still has more content past -- the usual "there's more below" cue for a
 * card whose body overflows and scrolls internally.
 */
@Composable
private fun ScrollFadeEdges(scrollState: ScrollState, modifier: Modifier = Modifier) {
    val canScrollBackward by remember { derivedStateOf { scrollState.value > 0 } }
    val canScrollForward by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }
    val edgeColor = MaterialTheme.colorScheme.surfaceVariant
    val fadeHeight = dimensionResource(id = R.dimen.padding_xxlarge)

    Box(modifier = modifier) {
        if (canScrollBackward) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(fadeHeight)
                    .background(Brush.verticalGradient(listOf(edgeColor, edgeColor.copy(alpha = 0f))))
            )
        }
        if (canScrollForward) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(fadeHeight)
                    .background(Brush.verticalGradient(listOf(edgeColor.copy(alpha = 0f), edgeColor)))
            )
        }
    }
}

/**
 * A [CardCarousel] centered in whatever space the caller gives it, at most 70% of the screen tall
 * (less if the space is shorter, so the dots always fit) -- the shared body of every full-screen
 * carousel (mechanism decks, market concept articles).
 */
@Composable
fun CenteredCardCarousel(
    pages: List<DeckPage>,
    modifier: Modifier = Modifier,
    enlargedText: Boolean = false,
    onNavigateToRoute: (String) -> Unit = {}
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val maxCardHeight = (screenHeight * 0.7f).coerceAtMost(maxHeight - 48.dp)
        CardCarousel(pages = pages, maxCardHeight = maxCardHeight, enlargedText = enlargedText, onNavigateToRoute = onNavigateToRoute)
    }
}

@Composable
private fun CarouselArrow(pointsForward: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(ArrowSize)
            .clip(CircleShape)
            .background(LocalPulseColors.current.accentPrimary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_forward),
            contentDescription = stringResource(
                id = if (pointsForward) R.string.carousel_next_card else R.string.carousel_previous_card
            ),
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.icon_size_small))
                .graphicsLayer(scaleX = if (pointsForward) 1f else -1f)
        )
    }
}

/**
 * Same morphing-pill treatment as the onboarding carousel's `PageIndicator`: each dot's selection is
 * how close [position] (the pager's current page plus its drag fraction) sits to its own index, so
 * mid-swipe the leaving dot shrinks back to a plain circle while the arriving one grows into a pill
 * and gains the accent color, both tracking the finger rather than snapping once the page settles.
 */
@Composable
private fun CarouselDots(pageCount: Int, position: Float) {
    val pulseColors = LocalPulseColors.current
    val dotSize = dimensionResource(id = R.dimen.onboarding_dot_size)
    val activeWidth = dimensionResource(id = R.dimen.onboarding_dot_active_width)
    val currentPage = position.roundToInt().coerceIn(0, pageCount - 1)
    val description = stringResource(id = R.string.deck_page_indicator_content_description, currentPage + 1, pageCount)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selection = (1f - abs(position - index)).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                    .height(dotSize)
                    .width(lerp(dotSize, activeWidth, selection))
                    .clip(CircleShape)
                    .background(lerp(pulseColors.onSurfaceMuted, pulseColors.accentPrimary, selection))
            )
        }
    }
}

private val ArrowSize = 30.dp

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true, heightDp = 640)
@Composable
private fun PreviewCardCarouselLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CardCarousel(pages = mechanismContentPages(Mechanism.TACTICAL_MOMENTUM, onSeeIndicators = {}), maxCardHeight = maxHeight * 0.7f)
        }
    }
}

@Preview(name = "Dark", showBackground = true, heightDp = 640)
@Composable
private fun PreviewCardCarouselDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CardCarousel(pages = mechanismContentPages(Mechanism.MACRO_VITALS, onSeeIndicators = {}), maxCardHeight = maxHeight * 0.7f)
        }
    }
}
