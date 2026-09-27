package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.learn.IndicatorArticlesProvider
import com.marketlabs.pulse.core.learn.LearnContentProvider
import com.marketlabs.pulse.ui.components.PulseBackTitleRow
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.PulseTabRow
import com.marketlabs.pulse.ui.components.tutorials.GaugePatternBandBar
import com.marketlabs.pulse.ui.components.tutorials.Mechanism
import com.marketlabs.pulse.ui.components.tutorials.TutorialsHubGrid
import com.marketlabs.pulse.ui.screens.stocks.detail.ViewMoreRow
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Fixed display order for the "Economic Events" tiles -- 6 of these ids are shared with
 * [TutorialsGaugesCatalog]'s own `macro_vitals` entries (the same indicator article, reached here a
 * second time as a standard release rather than as a gauge); the other 5 have no gauge at all and
 * exist only as this section's own articles. The 35 indicator articles proper live one level down,
 * inside each mechanism deck's own "See all indicators" ([TutorialsGaugesScreen]) -- not on this hub. */
private val ECONOMIC_EVENT_ARTICLE_KEYS = listOf(
    "cpi_yoy", "core_pce_yoy", "nfp", "unemployment", "real_gdp", "retail_sales",
    "ism_manufacturing_pmi", "ism_services_pmi", "ppi", "initial_jobless_claims", "fomc_rate_decision"
)

/** The three swipeable/tappable tabs, in display order -- each id is both this list's own tag and
 * the key into `learn_content.json`'s `hub_sections` map, so a tab's label and its page's own
 * subtitle share one source of copy. */
private val HUB_TAB_IDS = listOf("market_concepts", "economic_events", "mechanisms")

/**
 * Settings -> Tutorials, and the "Learn about Market" target of every screen's "?" sheet. Uses the
 * same collapsing-chrome + sticky-tab-row + swipeable-pager pattern as Stock Detail/Indicators (see
 * `docs/architecture/collapsing-header-tabs.md` before touching this) -- [PulseBackTitleRow] is the
 * only pinned zone; Getting Started (the Gauge Anatomy hero), About the Data (Data Limitations), and
 * the tab group's own header all live in one shared collapsing region that scrolls away together as
 * the reader scrolls into a tab, with [PulseTabRow] settling directly below the back row once it
 * has. [chromeScrollableState] exists because both the hero card and the Data Limitations row inside
 * that region are themselves clickable `PulseCard`s -- see the doc's "Bugs hit and fixes" #5.
 *
 * Market concepts splits into [ConceptSubGroup] sub-headings, each its own [TutorialsHubSubSectionCard]
 * wrapping a 2-column grid of [TutorialsHubGrid] (so a sub-section reads as one card, its tiles
 * nested cards inside it) -- the one tab with sub-headings, so it's the one that still needs that
 * outer card. Economic events and market mechanisms both have no sub-headings, so their tiles sit
 * directly on the page (no outer [TutorialsHubSubSectionCard]) -- economic events is a plain 2-column
 * grid of [ECONOMIC_EVENT_ARTICLE_KEYS] via [TutorialsHubGrid]; market mechanisms is
 * [MechanismIndicatorsGrid], its own 2-column grid of [MechanismGridTile]s -- each has two independent tap zones
 * unlike every other tile in this hub: the mechanism name (opens its [MechanismDeckScreen]) and a
 * trailing "Indicators" link ([ViewMoreRow]) that jumps straight to that mechanism's own "See all
 * indicators" ([TutorialsGaugesScreen]), bypassing the deck -- the same destination its deck's own
 * last card already links to, not a new one.
 *
 * Every section/tab's title/subtitle is looked up from `learn_content.json`'s `hub_sections` map by
 * a fixed id (`start`/`data`/`tabs`/[HUB_TAB_IDS]) hardcoded at each call site below -- there's no
 * enum backing this hub's own top-level section list, so the id is whatever this function itself
 * decides to look up.
 */
@Composable
fun TutorialsHubScreen(
    onNavigateUp: () -> Unit,
    onNavigateToGaugeAnatomy: () -> Unit,
    onNavigateToConcept: (ConceptArticle) -> Unit,
    onNavigateToMechanism: (Mechanism) -> Unit,
    onNavigateToIndicators: (Mechanism) -> Unit,
    onNavigateToDataLimitations: () -> Unit,
    onNavigateToArticle: (String) -> Unit
) {
    val context = LocalContext.current
    val learnContent = LearnContentProvider.get(context)
    val indicatorArticles = IndicatorArticlesProvider.get(context)
    val density = LocalDensity.current
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    val pagerState = rememberPagerState { HUB_TAB_IDS.size }
    val scope = rememberCoroutineScope()

    // 💡 Collapsing chrome state (Getting Started + About the Data + the tab group's own header),
    // shared across every tab, not per-tab -- scrolling it away on one tab keeps it collapsed when
    // you switch to another. `rememberSaveable`, not a plain `remember`, so a reader who scrolled
    // this away, then pushed into an article/deck and came back, finds it exactly as they left it
    // instead of fully re-expanded. See `docs/architecture/collapsing-header-tabs.md` piece #2.
    var chromeHeightPx by rememberSaveable { mutableFloatStateOf(0f) }
    var collapseOffsetPx by rememberSaveable { mutableFloatStateOf(0f) }

    val chromeNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y >= 0f) return Offset.Zero
                val newOffset = (collapseOffsetPx + available.y).coerceIn(-chromeHeightPx, 0f)
                val consumed = newOffset - collapseOffsetPx
                collapseOffsetPx = newOffset
                return Offset(0f, consumed)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y <= 0f) return Offset.Zero
                val newOffset = (collapseOffsetPx + available.y).coerceIn(-chromeHeightPx, 0f)
                val consumedNow = newOffset - collapseOffsetPx
                collapseOffsetPx = newOffset
                return Offset(0f, consumedNow)
            }
        }
    }

    // 💡 Gives a drag that starts directly on the hero card or the Data Limitations row (both
    // clickable `PulseCard`s inside the collapsing region below) a real scrollable ancestor of its
    // own, so touch-slop cancels their `onClick` on a genuine scroll instead of misfiring as a tap --
    // see the doc's "Bugs hit and fixes" #5. Mirrors `chromeNestedScrollConnection`'s exact clamp math.
    val chromeScrollableState = rememberScrollableState { delta ->
        val newOffset = (collapseOffsetPx + delta).coerceIn(-chromeHeightPx, 0f)
        val consumed = newOffset - collapseOffsetPx
        collapseOffsetPx = newOffset
        consumed
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom))
    ) {
        PulseBackTitleRow(title = stringResource(id = R.string.tutorials_hub_screen_title), onNavigateUp = onNavigateUp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { (chromeHeightPx + collapseOffsetPx).coerceIn(0f, chromeHeightPx).toDp() })
                .clipToBounds()
                .scrollable(state = chromeScrollableState, orientation = Orientation.Vertical)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
                        if (chromeHeightPx != placeable.height.toFloat()) {
                            chromeHeightPx = placeable.height.toFloat()
                        }
                        layout(placeable.width, 0) {
                            placeable.place(0, collapseOffsetPx.roundToInt())
                        }
                    }
                    .padding(horizontal = paddingLarge, vertical = paddingLarge),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_xlarge))
            ) {
                TutorialsHubSectionHeader(
                    text = learnContent.hubSections["start"]?.title.orEmpty(),
                    subtitle = learnContent.hubSections["start"]?.subtitle.orEmpty()
                )
                TutorialsHubHeroCard(
                    title = learnContent.gaugeAnatomy.title,
                    onClick = onNavigateToGaugeAnatomy
                )

                TutorialsHubSectionDivider()
                TutorialsHubSectionHeader(
                    text = learnContent.hubSections["data"]?.title.orEmpty(),
                    subtitle = learnContent.hubSections["data"]?.subtitle.orEmpty()
                )
                TutorialsHubRow(
                    label = learnContent.dataLimitations.title,
                    onClick = onNavigateToDataLimitations
                )

                TutorialsHubSectionDivider()
                TutorialsHubSectionHeader(
                    text = learnContent.hubSections["tabs"]?.title.orEmpty(),
                    subtitle = learnContent.hubSections["tabs"]?.subtitle.orEmpty()
                )
            }
        }

        PulseTabRow(
            tabs = HUB_TAB_IDS.map { learnContent.hubSections[it]?.title.orEmpty() },
            selectedTabIndex = pagerState.currentPage,
            onTabSelected = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
            selectionPosition = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
            modifier = Modifier.fillMaxWidth().padding(horizontal = paddingLarge)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .nestedScroll(chromeNestedScrollConnection)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val tabId = HUB_TAB_IDS[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingLarge),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_xlarge))
                ) {
                    when (tabId) {
                        "market_concepts" -> ConceptSubGroup.entries.forEach { subGroup ->
                            val articles = ConceptArticle.entries.filter { it.subGroup == subGroup }
                            val heading = learnContent.conceptSubgroupHeadings[subGroup.jsonKey]
                            TutorialsHubSubSectionCard {
                                TutorialsHubSubHeader(
                                    text = heading?.title.orEmpty(),
                                    subtitle = heading?.subtitle.orEmpty()
                                )
                                TutorialsHubGrid(
                                    items = articles,
                                    title = { learnContent.conceptArticles[it.routeKey]?.title.orEmpty() },
                                    subtitle = { learnContent.conceptArticles[it.routeKey]?.subtitle.orEmpty() },
                                    onClick = onNavigateToConcept
                                )
                            }
                        }
                        "economic_events" -> TutorialsHubGrid(
                            items = ECONOMIC_EVENT_ARTICLE_KEYS,
                            title = { indicatorArticles[it]?.title.orEmpty() },
                            subtitle = { indicatorArticles[it]?.subtitle.orEmpty() },
                            onClick = onNavigateToArticle
                        )
                        "mechanisms" -> MechanismIndicatorsGrid(
                            mechanisms = Mechanism.entries,
                            subtitleFor = { learnContent.mechanismDecks[it.routeKey]?.subtitle.orEmpty() },
                            onNavigateToMechanism = onNavigateToMechanism,
                            onNavigateToIndicators = onNavigateToIndicators
                        )
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))
                }
            }
        }
    }
}

@Composable
private fun TutorialsHubSectionHeader(text: String, subtitle: String) {
    Column {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = LocalPulseColors.current.accentPrimary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = LocalPulseColors.current.onSurfaceMuted,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_tiny))
        )
    }
}

/** A hairline rule between two blocks inside the collapsing chrome (Getting Started / About the
 * Data / the tab group's own header) -- `spacedBy` on the parent Column already puts a gap on both
 * sides of it, so this is just the line itself. */
@Composable
private fun TutorialsHubSectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small)),
        thickness = dimensionResource(id = R.dimen.border_thin)
    )
}

/** A small uppercase label grouping a run of tiles under a [TutorialsHubSectionHeader], one rung
 * below it -- same `accentPrimary` title color as the section header above it, just one type-scale
 * step down, so a sub-section still reads as part of the same heading family. */
@Composable
private fun TutorialsHubSubHeader(text: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = LocalPulseColors.current.accentPrimary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = LocalPulseColors.current.onSurfaceMuted,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_tiny))
        )
    }
}

/** Wraps a whole [ConceptSubGroup]'s sub-header + grid in one outer `PulseCard` -- the grid's own
 * tiles stay their own individual cards inside it (nested cards), same as the per-item styling
 * already used everywhere else in this hub. */
@Composable
private fun TutorialsHubSubSectionCard(content: @Composable ColumnScope.() -> Unit) {
    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = R.dimen.padding_large)),
            content = content
        )
    }
}

/** The one card in "Getting Started" -- deliberately larger/bolder than a [TutorialsHubGridTile],
 * full width, and positioned above every grid so the reader hits it first. Carries its own preview
 * strip (a small [GaugePatternBandBar], the same band-with-marker visual the article's first card
 * teaches) -- the one place in the hub that keeps a preview at all, per the grid decision that gave
 * every other tile plain title+subtitle text instead. */
@Composable
private fun TutorialsHubHeroCard(title: String, onClick: () -> Unit) {
    PulseCard(style = PulseCardStyle.DATA, onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = R.dimen.padding_xlarge))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = null,
                    tint = LocalPulseColors.current.accentPrimary
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            GaugePatternBandBar(
                bullFraction = 1f / 3f,
                neutralFraction = 1f / 3f,
                bearFraction = 1f / 3f,
                markerFraction = 0.5f
            )
        }
    }
}

@Composable
private fun TutorialsHubRow(label: String, onClick: () -> Unit) {
    PulseCard(style = PulseCardStyle.DATA, onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_large)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_forward),
                contentDescription = null,
                tint = LocalPulseColors.current.accentPrimary
            )
        }
    }
}

/** Same 2-column/odd-item-full-row grid shape as [TutorialsHubGrid], rendering [MechanismGridTile]
 * instead of the generic [TutorialsHubGridTile] -- kept as its own small function rather than
 * generalizing the shared grid, since [TutorialsHubGrid] has two other call sites that don't need a
 * second tap zone per tile. */
@Composable
private fun MechanismIndicatorsGrid(
    mechanisms: List<Mechanism>,
    subtitleFor: (Mechanism) -> String,
    onNavigateToMechanism: (Mechanism) -> Unit,
    onNavigateToIndicators: (Mechanism) -> Unit
) {
    val pairedCount = if (mechanisms.size % 2 == 1) mechanisms.size - 1 else mechanisms.size
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))) {
        mechanisms.take(pairedCount).chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
            ) {
                pair.forEach { mechanism ->
                    MechanismGridTile(
                        mechanism = mechanism,
                        subtitle = subtitleFor(mechanism),
                        onNavigateToMechanism = onNavigateToMechanism,
                        onNavigateToIndicators = onNavigateToIndicators,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }
        if (mechanisms.size % 2 == 1) {
            val last = mechanisms.last()
            MechanismGridTile(
                mechanism = last,
                subtitle = subtitleFor(last),
                onNavigateToMechanism = onNavigateToMechanism,
                onNavigateToIndicators = onNavigateToIndicators
            )
        }
    }
}

/** One tile in [MechanismIndicatorsGrid] -- unlike every other tile in this hub, this has two
 * independent tap zones: the mechanism name + chevron up top (its own [MechanismDeckScreen]) and a
 * trailing "Indicators" link ([ViewMoreRow], the app's standard "see more" affordance) at the
 * bottom, right-aligned, that jumps straight to that mechanism's own "See all indicators"
 * ([TutorialsGaugesScreen]) -- a shortcut past the deck, not a new destination. Same
 * `PulseCard(DATA)` + `border_thin`/`accentSurfaceBorder` treatment as [TutorialsHubGridTile], just
 * with no single `onClick` on the card itself since the two zones need independent click handling. */
@Composable
private fun MechanismGridTile(
    mechanism: Mechanism,
    subtitle: String,
    onNavigateToMechanism: (Mechanism) -> Unit,
    onNavigateToIndicators: (Mechanism) -> Unit,
    modifier: Modifier = Modifier
) {
    val mechanismTitle = stringResource(id = mechanism.titleRes)
    val tileShape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large))
    PulseCard(
        style = PulseCardStyle.DATA,
        shape = tileShape,
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .border(
                border = BorderStroke(dimensionResource(id = R.dimen.border_thin), LocalPulseColors.current.accentSurfaceBorder),
                shape = tileShape
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = R.dimen.padding_large))) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMechanism(mechanism) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = mechanismTitle,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = null,
                    tint = LocalPulseColors.current.accentPrimary
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LocalPulseColors.current.onSurfaceMuted,
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            ViewMoreRow(
                text = stringResource(id = R.string.tutorials_hub_mechanism_indicators_link),
                onClick = { onNavigateToIndicators(mechanism) },
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewTutorialsHubScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        TutorialsHubScreen(
            onNavigateUp = {},
            onNavigateToGaugeAnatomy = {},
            onNavigateToConcept = {},
            onNavigateToMechanism = {},
            onNavigateToIndicators = {},
            onNavigateToDataLimitations = {},
            onNavigateToArticle = {}
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewTutorialsHubScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        TutorialsHubScreen(
            onNavigateUp = {},
            onNavigateToGaugeAnatomy = {},
            onNavigateToConcept = {},
            onNavigateToMechanism = {},
            onNavigateToIndicators = {},
            onNavigateToDataLimitations = {},
            onNavigateToArticle = {}
        )
    }
}
