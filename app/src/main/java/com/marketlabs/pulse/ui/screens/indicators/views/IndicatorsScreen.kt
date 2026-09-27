package com.marketlabs.pulse.ui.screens.indicators.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.DomainAiSynthesis
import com.marketlabs.pulse.storage.model.indicators.DomainExecutiveBlock
import com.marketlabs.pulse.storage.model.indicators.DomainHorizons
import com.marketlabs.pulse.storage.model.indicators.DomainIndicatorPillar
import com.marketlabs.pulse.storage.model.indicators.DomainPillarScorecardEntry
import com.marketlabs.pulse.storage.model.indicators.DomainShift
import com.marketlabs.pulse.storage.model.indicators.DomainUnifiedMetric
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.ui.components.AnalyzedAtHeader
import com.marketlabs.pulse.ui.components.DisclaimerFooter
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.PulseTabRow
import com.marketlabs.pulse.ui.components.UniversalMetricCard
import com.marketlabs.pulse.ui.components.rememberPerTabLazyListStates
import com.marketlabs.pulse.ui.components.widgets.AiGeneratedLabel
import com.marketlabs.pulse.ui.components.widgets.CardEyebrowLabel
import com.marketlabs.pulse.ui.components.widgets.MetricInfoAction
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.pillColor
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.AgreementState
import com.marketlabs.pulse.utils.enums.AlignmentState
import com.marketlabs.pulse.utils.enums.IndicatorCategory
import com.marketlabs.pulse.utils.enums.ShiftDirection
import com.marketlabs.pulse.utils.enums.SignalColor
import com.marketlabs.pulse.utils.enums.SubcategoryEnums
import com.marketlabs.pulse.utils.extensions.smartTitleCase
import kotlin.math.roundToInt

/**
 * The 4 pillars are one [PulseTabRow] tab apiece -- Tactical Momentum, Systemic Risk, Valuation,
 * Macro Vitals, in that order (same shape as Insights' 4 sections, see `InsightsScreen.kt`'s
 * doc comment). The
 * "Analyzed as of" timestamp, the AI executive briefing, and the Horizons nav card stay shared
 * chrome above the tab row -- they're general context, not specific to any one pillar -- with the
 * same collapse-then-stick behavior Stock Detail's Deep Dive banner/tab row use (see
 * `StockDetailRoute.kt`'s identical `NestedScrollConnection`/`Modifier.layout` shape): scroll up
 * inside a tab's content and this chrome collapses away first, parking the tab row at the top;
 * scroll back down at a tab's own top and it reopens before pull-to-refresh engages.
 */
enum class IndicatorsTab(val labelRes: Int) {
    // 💡 Short tab labels (`indicators_tab_*`) are deliberately distinct from the `pillar_*`
    // strings `PillarUIConfig.title` uses for each tab's own inner section heading -- "Momentum"
    // on the tab bar, "Tactical Momentum" once you're on that tab, same idea as a nav label vs. a
    // page title.
    //
    // 💡 FAVORITES is first in display order (same as `StockAnalysisTab`'s own Favorites-first
    // layout), but the default landing tab stays `TACTICAL_MOMENTUM` -- `IndicatorsViewModel`
    // initializes `_selectedTabIndex` to `TACTICAL_MOMENTUM.ordinal`, not `0`, for exactly this
    // reason (display order and default tab are deliberately independent).
    FAVORITES(R.string.indicators_tab_favorites),
    TACTICAL_MOMENTUM(R.string.indicators_tab_momentum),
    SYSTEMIC_RISK(R.string.indicators_tab_systemic_risk),
    VALUATION(R.string.indicators_tab_valuation),
    MACRO_VITALS(R.string.indicators_tab_macro)
}

// ============================================================================
// 📱 MASTER STATE CONTROLLER
// ============================================================================
@Composable
fun IndicatorsScreen(
    data: MarketIndicators,
    scaffoldPadding: PaddingValues,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    favoriteMetricIds: Set<String>,
    onToggleFavoriteMetric: (String) -> Unit,
    onNavigateToHorizons: () -> Unit,
    onNavigateToMetricDetail: (String) -> Unit
) {
    // 💡 Every metric across all 4 pillars, flattened once per composition -- backs both
    // `metricNames` (the shift-row id -> display-name lookup below) and the Favorites tab, which
    // needs the full `DomainUnifiedMetric` (not just a name) for whichever ids are favorited,
    // regardless of which pillar they came from.
    val allMetrics = remember(data) {
        listOfNotNull(data.tacticalMomentum, data.systemicRisk, data.valuation, data.macroVitals)
            .flatMap { it.metrics }
    }
    // 💡 metric_id -> display name. `executive.shifts[]` only ever carries a metric_id string --
    // the backend deliberately keeps that cross-reference a UI-layer concern (validated
    // server-side, but never resolved to a display name server-side) so this app can render
    // whatever name it's already showing on that metric's own card.
    val metricNames = remember(allMetrics) { allMetrics.associate { it.id to it.name } }

    IndicatorsMainFeed(
        data = data,
        allMetrics = allMetrics,
        metricNames = metricNames,
        scaffoldPadding = scaffoldPadding,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = onTabSelected,
        favoriteMetricIds = favoriteMetricIds,
        onToggleFavoriteMetric = onToggleFavoriteMetric,
        onShowHorizons = onNavigateToHorizons,
        onNavigateToMetricDetail = onNavigateToMetricDetail
    )
}

// ============================================================================
// 📜 MAIN MINIMALIST FEED
// ============================================================================
@Composable
private fun IndicatorsMainFeed(
    data: MarketIndicators,
    allMetrics: List<DomainUnifiedMetric>,
    metricNames: Map<String, String>,
    scaffoldPadding: PaddingValues,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    favoriteMetricIds: Set<String>,
    onToggleFavoriteMetric: (String) -> Unit,
    onShowHorizons: () -> Unit,
    onNavigateToMetricDetail: (String) -> Unit
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val density = LocalDensity.current

    val pagerState = rememberPagerState(initialPage = selectedTabIndex) { IndicatorsTab.entries.size }
    // 💡 One `LazyListState` per tab, hoisted above the pager like every other `PulseTabRow` +
    // `HorizontalPager` screen in this app (`StockAnalysisScreen`, `StockDetailScreen`,
    // `InsightsScreen`) -- see `rememberPerTabLazyListStates`'s own doc comment for why this needs
    // to be `rememberSaveable`, not a plain `remember`.
    val lazyListStates = rememberPerTabLazyListStates(IndicatorsTab.entries.size)

    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }

    // 💡 `settledPage`, not `currentPage` -- see StockDetailRoute.kt/InsightsRoute.kt's identical
    // fix for why: keying off `currentPage` pushes an intermediate, still-in-flight page value
    // back to the caller mid-swipe/mid-animation, which the effect above then reads and uses to
    // correct the pager, fighting whatever gesture/animation is still running.
    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage != selectedTabIndex) {
            onTabSelected(pagerState.settledPage)
        }
    }

    // 💡 Collapsing chrome state (timestamp + AI briefing + Horizons card), shared across every
    // tab, not per-tab -- scrolling it away on one tab keeps it collapsed when you switch to
    // another. Identical shape to `StockDetailRoute.kt`'s Deep Dive banner state -- see that file
    // for the full reasoning on why `chromeHeightPx` is measured via the `Modifier.layout`
    // override below rather than a plain `onGloballyPositioned`.
    //
    // 💡 `rememberSaveable`, not a plain `remember`, for the same reason as `lazyListStates` above --
    // otherwise a reader who scrolled this chrome away, then tapped into a metric's detail screen
    // and back, would find it fully re-expanded (both values reset to 0f), pushing the tab content
    // they were reading down to a different position than where they left it even though the tab's
    // own scroll offset was correctly restored.
    var chromeHeightPx by rememberSaveable { mutableStateOf(0f) }
    var collapseOffsetPx by rememberSaveable { mutableStateOf(0f) }

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

    // 💡 The chrome region itself (below) has no scrollable ancestor of its own -- only the
    // pager's Box (`chromeNestedScrollConnection`, attached further down) receives scroll deltas,
    // and only once they're dispatched *from* the LazyColumn inside the pager. A drag that starts
    // directly on `AiExecutiveBriefingHero` (once expanded, tall enough to want scrolling past)
    // would have nothing to claim it as a scroll, so `PulseCard`'s plain `Modifier.clickable` (see
    // `PulseCard.kt`) would never get its tap cancelled by touch-slop the way it does for
    // `SynthesisHeroCard`/`MarketSentimentCard`, both of which sit inside a real `LazyColumn.item{}`
    // -- so the drag-to-scroll gesture would fire `onClick` on release, snapping the card straight
    // back to collapsed. This gives the chrome region its own real `scrollable`, mirroring the exact
    // same clamp math `chromeNestedScrollConnection` uses, so a drag beginning here is claimed by
    // touch-slop like any other scrollable ancestor and doesn't misfire as a tap.
    val chromeScrollableState = rememberScrollableState { delta ->
        val newOffset = (collapseOffsetPx + delta).coerceIn(-chromeHeightPx, 0f)
        val consumed = newOffset - collapseOffsetPx
        collapseOffsetPx = newOffset
        consumed
    }

    val pillarConfigByTab = mapOf(
        IndicatorsTab.TACTICAL_MOMENTUM to data.tacticalMomentum?.let {
            PillarUIConfig(
                stringResource(id = R.string.pillar_tactical_momentum),
                IndicatorCategory.TACTICAL_MOMENTUM,
                it,
                description = stringResource(id = R.string.pillar_tactical_momentum_description)
            )
        },
        IndicatorsTab.SYSTEMIC_RISK to data.systemicRisk?.let {
            PillarUIConfig(
                stringResource(id = R.string.pillar_systemic_risk),
                IndicatorCategory.SYSTEMIC_RISK,
                it,
                description = stringResource(id = R.string.pillar_systemic_risk_description)
            )
        },
        IndicatorsTab.VALUATION to data.valuation?.let {
            PillarUIConfig(
                stringResource(id = R.string.pillar_valuation),
                IndicatorCategory.VALUATION,
                it,
                description = stringResource(id = R.string.pillar_valuation_description)
            )
        },
        IndicatorsTab.MACRO_VITALS to data.macroVitals?.let {
            // 💡 FLAGGED AS MACRO: This allows us to conditionally render the release dates
            PillarUIConfig(
                stringResource(id = R.string.pillar_macro_vitals),
                IndicatorCategory.MACRO_ECONOMY,
                it,
                isMacro = true,
                description = stringResource(id = R.string.pillar_macro_vitals_description)
            )
        }
    )

    val scorecardByPillar = data.aiSynthesis?.pillarScorecard?.associateBy { it.pillar } ?: emptyMap()

    // 💡 No icon+"Market Indicators" row opens this screen -- the title lives in the global top bar
    // (MainActivity resolves it per-route), so a row here would say the same thing twice in two
    // places on screen at once. `scaffoldPadding`'s top component (not the raw status bar inset
    // alone) is what actually accounts for the top bar's real rendered height, so content starts
    // right below it instead of underneath it.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = scaffoldPadding.calculateTopPadding())
    ) {
        // 💡 Pinned, not part of the collapsing chrome below -- stays on screen the whole time,
        // same role `DetailHeader` plays on Stock Detail. Only the timestamp is pinned; Today's
        // Read (the AI briefing card) and the Horizons card both live in the collapsing region
        // below, scrolling away together until the tab row settles directly beneath this.
        data.aiSynthesis?.timestamp?.let { timestamp ->
            AnalyzedAtHeader(timestamp = timestamp, modifier = Modifier.padding(horizontal = paddingLarge))
            Spacer(modifier = Modifier.height(paddingLarge))
        }

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
                        // 💡 Measured with height unbounded, ignoring whatever (possibly zero on
                        // the very first frame, before `chromeHeightPx` is known) height constraint
                        // the shrinking Box above passes down -- otherwise this Column would be
                        // squeezed to match it and could never report its true height back. Today's
                        // Read (below) can grow a lot when expanded (whatChanged + shifts, no
                        // internal cap of its own -- see that section's own comment on why not);
                        // this always measures its true size regardless, and `weight(1f)` on the
                        // pager below is what keeps that safe rather than a cap here.
                        val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
                        if (chromeHeightPx != placeable.height.toFloat()) {
                            chromeHeightPx = placeable.height.toFloat()
                        }
                        // 💡 Reports zero size upward -- this node's own contribution to the parent
                        // Box's size is deliberately nothing; that Box's height is fully driven by
                        // `chromeHeightPx`/`collapseOffsetPx` state instead, not by this child.
                        layout(placeable.width, 0) {
                            placeable.place(0, collapseOffsetPx.roundToInt())
                        }
                    }
                    .padding(horizontal = paddingLarge)
            ) {
                AiExecutiveBriefingHero(
                    executive = data.aiSynthesis?.executive,
                    timestamp = data.aiSynthesis?.timestamp,
                    metricNames = metricNames
                )
                Spacer(modifier = Modifier.height(paddingLarge))

                HorizonNavigationCard(onClick = onShowHorizons)
                Spacer(modifier = Modifier.height(paddingLarge))
            }
        }

        PulseTabRow(
            tabs = IndicatorsTab.entries.map { stringResource(id = it.labelRes) },
            selectedTabIndex = selectedTabIndex,
            onTabSelected = onTabSelected,
            highlightedTabIndex = IndicatorsTab.FAVORITES.ordinal,
            selectionPosition = { pagerState.currentPage + pagerState.currentPageOffsetFraction }
        )

        // 💡 `weight(1f)`, not just `fillMaxSize()` -- without it, if the collapsing chrome above
        // (Today's Read, which can expand to show what_changed + shifts[] with no cap of its own,
        // plus the Horizons card) ever measured taller than the remaining screen height, a plain
        // Column doesn't shrink earlier children to fit; it lets everything overflow, pushing this
        // Box (and the only scrollable content in it) off-screen with no way to reach it to scroll
        // back. `weight(1f)` guarantees this always gets whatever space is actually left, even a
        // thin sliver -- still enough to catch a scroll gesture and collapse the chrome back down
        // via `chromeNestedScrollConnection`, restoring the rest of the space. Same fix as
        // `StockDetailRoute.kt`'s identical `weight(1f)`.
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
                val tab = IndicatorsTab.entries[page]
                val config = pillarConfigByTab[tab]
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListStates[tab.ordinal],
                    contentPadding = PaddingValues(
                        start = paddingLarge,
                        end = paddingLarge,
                        top = paddingLarge,
                        bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge
                    ),
                    // 💡 padding_extra_large gap before the footer -- same value every other
                    // screen's DisclaimerFooter sits below; this LazyColumn only ever has the one
                    // content item plus the footer, so spacedBy here affects just that one gap.
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_extra_large))
                ) {
                    if (tab == IndicatorsTab.FAVORITES) {
                        val favoritedMetrics = allMetrics.filter { it.id in favoriteMetricIds }
                        if (favoritedMetrics.isEmpty()) {
                            item { IndicatorsFavoritesEmptyState() }
                        } else {
                            item {
                                FavoritesSection(
                                    metrics = favoritedMetrics,
                                    favoriteMetricIds = favoriteMetricIds,
                                    onToggleFavorite = onToggleFavoriteMetric,
                                    onIndicatorClick = { metric -> onNavigateToMetricDetail(metric.id) }
                                )
                            }
                        }
                    } else if (config != null) {
                        item {
                            PillarSection(
                                config = config,
                                scorecardEntry = scorecardByPillar[config.pillarCategory],
                                favoriteMetricIds = favoriteMetricIds,
                                onToggleFavorite = onToggleFavoriteMetric,
                                onIndicatorClick = { metric -> onNavigateToMetricDetail(metric.id) }
                            )
                        }
                    } else {
                        item { IndicatorsTabEmptyState() }
                    }

                    item { DisclaimerFooter() }
                }
            }
        }
    }
}

/** Same "nothing loaded yet" treatment `InsightsScreen`/`NewsScreen` use for their own empty states. */
@Composable
private fun LazyItemScope.IndicatorsTabEmptyState() {
    Box(
        modifier = Modifier.fillParentMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.indicators_tab_empty_state),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Same shape as [IndicatorsTabEmptyState], distinct copy -- "nothing tracked" vs. "nothing starred". */
@Composable
private fun LazyItemScope.IndicatorsFavoritesEmptyState() {
    Box(
        modifier = Modifier.fillParentMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.indicators_favorites_empty_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
        )
    }
}

/**
 * The Favorites tab's content -- a flat 2-per-row grid of whichever metrics are favorited,
 * regardless of which pillar they came from (no subcategory grouping, unlike [PillarSection] --
 * a cross-pillar list doesn't have one pillar's subcategory taxonomy to group by). Cards look
 * identical to their own pillar tab's version -- the Favorites tab's visual distinction lives one
 * level up, on the `PulseTabRow` chip itself (`highlightedTabIndex`), not on the cards inside it.
 */
@Composable
private fun FavoritesSection(
    metrics: List<DomainUnifiedMetric>,
    favoriteMetricIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onIndicatorClick: (DomainUnifiedMetric) -> Unit
) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = paddingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_star_filled),
                contentDescription = null,
                tint = LocalPulseColors.current.accentPrimary,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = stringResource(id = R.string.indicators_tab_favorites),
                style = textStyle,
                color = LocalPulseColors.current.accentPrimary
            )
        }

        metrics.chunked(2).forEach { rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(paddingMedium)
            ) {
                rowMetrics.forEach { metric ->
                    val formattedChange = metric.changeDisplay?.let { changeStr ->
                        if (metric.changeRaw == 0.0 && !changeStr.startsWith("+") && !changeStr.startsWith("-")) {
                            "+$changeStr"
                        } else {
                            changeStr
                        }
                    }
                    UniversalMetricCard(
                        title = metric.name,
                        value = metric.valueDisplay,
                        changeString = formattedChange,
                        signalText = metric.signalText,
                        signalColor = metric.signalColor,
                        // 💡 Same macro-only gating `PillarSection` applies via `config.isMacro` --
                        // release dates are a Macro Vitals concept, stripped from every other
                        // pillar's cards there, so a favorited non-macro metric shouldn't grow one
                        // back just because it's cross-pillar here.
                        dateString = if (IndicatorCategory.fromString(metric.category) == IndicatorCategory.MACRO_ECONOMY) metric.releaseDate else null,
                        isFavorite = metric.id in favoriteMetricIds,
                        onFavoriteClick = { onToggleFavorite(metric.id) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onIndicatorClick(metric) }
                    )
                }
                if (rowMetrics.size == 1) {
                    Spacer(modifier = Modifier.weight(1f).fillMaxHeight())
                }
            }
            Spacer(modifier = Modifier.height(paddingMedium))
        }
    }
}

// ============================================================================
// 🧱 SUB-COMPONENTS FOR MAIN FEED
// ============================================================================

@Composable
private fun AiExecutiveBriefingHero(
    executive: DomainExecutiveBlock?,
    timestamp: Long?,
    metricNames: Map<String, String>
) {
    if (executive == null || executive.headline.isBlank() || timestamp == null) return

    var isExpanded by remember { mutableStateOf(false) }

    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)

    // 💡 SYNTHESIS style -- this is the AI Executive Briefing, the same kind of AI-interpreted
    // content as Dashboard's Technical Briefing and the News cards.
    //
    // 💡 `animateContentSize()` sits on the inner `Column` below, not on `PulseCard`'s own outer
    // `modifier` -- `PulseCard` draws its shadow via `Modifier.shadow`, and `animateContentSize()`
    // clips whatever it wraps to its own animated rectangle each frame. Placed outside the shadow
    // (on the Card's own modifier), that rectangular clip cut straight through the shadow's rounded
    // corners, leaving a flat greyish sliver poking out past the bottom edge instead of a clean
    // rounded shadow.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(paddingLarge).animateContentSize()) {
            // 💡 This eyebrow (icon + label) marks the card as AI-sourced, the same role
            // Dashboard's Technical Briefing eyebrow plays -- accentPrimary, same shared
            // CardEyebrowLabel tier "Market Read"/"Where Capital's Moving" (Summary) and
            // Digest (Insights' SynthesisHeroCard) use. The "Analyzed as of" timestamp isn't in
            // the card at all -- see AnalyzedAtHeader, the first item in the screen's own
            // LazyColumn, matching how every other screen in this app (Summary's HeaderSection,
            // for instance) places its own timestamp at the top of the content, not nested
            // inside a card.
            //
            // 💡 The expand/collapse arrow sits beside the headline (explicitly sized), not in the
            // eyebrow row: an arrow in that row would force the row's height to the icon's own
            // default (unsized) size rather than the eyebrow text's -- taller than Market Signal's
            // bare eyebrow line right above it, and the extra slack would push both the top padding
            // and the eyebrow-to-pill gap out of line with Market Signal even though the Spacer
            // values below are identical. Matches Market Sentiment's chevron-on-the-headline
            // pattern (SummaryScreen.kt) -- the eyebrow row is just the label, same as Market
            // Signal/Sentiment.
            CardEyebrowLabel(
                text = stringResource(id = R.string.indicators_todays_read),
                color = LocalPulseColors.current.accentPrimary,
                iconRes = R.drawable.ic_ai_sparkle_filled,
                iconContentDescription = "Analysis Engine"
            )

            // 💡 Card order: alignment_with_macro pill, then headline, then alignment_note --
            // the code-computed read of whether the pillars agree with the macro regime frames
            // the headline, and the AI's own explanation of that read follows the headline
            // rather than sitting bundled with the pill above it.
            Spacer(modifier = Modifier.height(paddingMedium))
            SignalPill(
                text = executive.alignmentWithMacro.label,
                pillColor = executive.alignmentWithMacro.pillColor,
                contentColor = executive.alignmentWithMacro.textColor,
                outlined = true
            )

            Spacer(modifier = Modifier.height(paddingMedium))

            // 💡 The headline is the primary heading of this card -- always shown in full, never
            // clamped, the same way a news headline would be. The expand/collapse arrow sits
            // beside it, sized to `padding_large` like Market Sentiment's own chevron, rather than
            // left at Icon's default size.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = executive.headline.smartTitleCase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(paddingSmall))
                Icon(
                    painter = painterResource(id = if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(paddingLarge)
                )
            }

            if (executive.alignmentNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(paddingMedium))
                Text(
                    text = executive.alignmentNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 💡 what_changed and shifts[] are "extra metadata" -- collapsed hides them entirely
            // (nothing renders between alignment_note and the AI label below), expanded shows them
            // here, BETWEEN alignment_note and the label. Card order either way: alignment pill,
            // headline, alignment_note, [extra metadata if expanded], this label (always visible).
            // default along with the rest of the card's supporting detail, not shown until the
            // reader taps to expand. No internal scroll/height cap here -- the whole card (and
            // this whole screen's collapsing chrome, which it's part of) grows to show it in full;
            // the page as a whole scrolls normally to reveal whatever that pushes further down,
            // the same way a tall item in any list is reached by scrolling the list, not by
            // scrolling inside the item itself.
            if (isExpanded) {
                if (executive.whatChanged.isNotBlank()) {
                    Spacer(modifier = Modifier.height(paddingMedium))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
                    Spacer(modifier = Modifier.height(paddingMedium))
                    Text(
                        text = executive.whatChanged,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }

                if (executive.shifts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(paddingMedium))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
                    // 💡 No inner card/tinted Surface per shift -- same "no cards nested inside
                    // cards" fix already applied to WatchList/ForwardCalls/Scenarios: each shift is
                    // a plain block, separated from its neighbor by a full-width divider inside
                    // this one Today's Read card, not its own boxed row.
                    Column {
                        executive.shifts.forEachIndexed { index, shift ->
                            ShiftRow(
                                shift = shift,
                                metricName = metricNames[shift.metricId] ?: shift.metricId,
                                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
                            )
                            if (index != executive.shifts.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    thickness = dimensionResource(id = R.dimen.border_thin)
                                )
                            }
                        }
                    }
                }
            }

            if (executive.alignmentNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(paddingMedium))
                AiGeneratedLabel()
            }
        }
    }
}

@Composable
private fun ShiftRow(shift: DomainShift, metricName: String, modifier: Modifier = Modifier) {
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = metricName,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(paddingSmall))
            SignalPill(
                text = shift.direction.name,
                pillColor = shift.direction.pillColor,
                contentColor = shift.direction.textColor,
                outlined = true
            )
        }
        Text(
            text = shift.note,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HorizonNavigationCard(onClick: () -> Unit) {
    // 💡 SYNTHESIS style -- an AI-sourced entry point, same card family as the executive briefing
    // above it.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_large)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                CardEyebrowLabel(
                    text = stringResource(id = R.string.indicators_horizons_title),
                    color = LocalPulseColors.current.accentPrimary,
                    iconRes = R.drawable.ic_ai_sparkle_filled,
                    iconContentDescription = "Analysis Engine"
                )
                // 💡 `padding_medium` + 1.2x line height -- was `padding_tiny` (2dp) with no line
                // height override, out of step with every other AI-card's heading-to-body gap and
                // body-text treatment (Today's Read's alignmentNote/whatChanged, the pillar
                // scorecard's oneLiner, Horizon cards' whatThisMeans/watchFor all use this same
                // pairing).
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                Text(
                    text = stringResource(id = R.string.indicators_horizons_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_forward),
                contentDescription = null,
                tint = LocalPulseColors.current.accentPrimary,
                modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large))
            )
        }
    }
}

data class PillarUIConfig(
    val title: String,
    val pillarCategory: IndicatorCategory,
    val pillarData: DomainIndicatorPillar,
    val isMacro: Boolean = false,
    val description: String = ""
)

@Composable
private fun PillarSection(
    config: PillarUIConfig,
    scorecardEntry: DomainPillarScorecardEntry?,
    favoriteMetricIds: Set<String> = emptySet(),
    onToggleFavorite: ((String) -> Unit)? = null,
    onIndicatorClick: (DomainUnifiedMetric) -> Unit
) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = paddingLarge),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_engine_quant),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = config.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            MetricInfoAction(
                title = config.title,
                description = config.description
            )
        }

        scorecardEntry?.let { entry ->
            PillarScorecardCard(
                entry = entry,
                modifier = Modifier.padding(bottom = paddingMedium)
            )
        }

        val groupedMetrics = config.pillarData.metrics.groupBy { it.subcategory }

        groupedMetrics.forEach { (subcatEnum, metrics) ->

            if (subcatEnum != null) {
                val subcategoryText = when (subcatEnum) {
                    SubcategoryEnums.INFLATION -> stringResource(id = R.string.subcategory_inflation)
                    SubcategoryEnums.LABOR -> stringResource(id = R.string.subcategory_labor)
                    SubcategoryEnums.GROWTH -> stringResource(id = R.string.subcategory_growth)
                    SubcategoryEnums.POLICY -> stringResource(id = R.string.subcategory_policy)
                    else -> subcatEnum.label
                }

                // 💡 UPDATED: Typography changed to match native headers visually without uppercase labels
                Text(
                    text = subcategoryText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium), top = paddingLarge)
                )
            }

            metrics.chunked(2).forEach { rowMetrics ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(paddingMedium)
                ) {
                    rowMetrics.forEach { metric ->

                        val formattedChange = metric.changeDisplay?.let { changeStr ->
                            if (metric.changeRaw == 0.0 && !changeStr.startsWith("+") && !changeStr.startsWith("-")) {
                                "+$changeStr"
                            } else {
                                changeStr
                            }
                        }

                        UniversalMetricCard(
                            title = metric.name,
                            value = metric.valueDisplay,
                            changeString = formattedChange,
                            signalText = metric.signalText,
                            signalColor = metric.signalColor,
                            // 💡 HIDDEN: Evaluates macro flag to strip dates from pure technicals
                            dateString = if (config.isMacro) metric.releaseDate else null,
                            isFavorite = metric.id in favoriteMetricIds,
                            onFavoriteClick = onToggleFavorite?.let { toggle -> { toggle(metric.id) } },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { onIndicatorClick(metric) }
                        )
                    }
                    if (rowMetrics.size == 1) {
                        Spacer(modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
                Spacer(modifier = Modifier.height(paddingMedium))
            }
        }
    }
}

/**
 * Code-computed pillar-level rollup (`pillar_scorecard[]`) -- `agreement` (how much this pillar's
 * own metrics agree with each other) on the left, `stance` (this pillar's own color, same
 * [SignalColor] as its individual metric cards) on the right as a pill, `oneLiner` narrating the
 * shape below.
 */
@Composable
private fun PillarScorecardCard(
    entry: DomainPillarScorecardEntry,
    modifier: Modifier = Modifier
) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)

    // 💡 SYNTHESIS style -- same AI-sourced card family as the executive briefing hero and the
    // Horizons entry card.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(paddingLarge)) {
            // 💡 Same eyebrow (sparkle icon + accent label) as the executive briefing hero's
            // "Today's Read" -- marks this card as AI-sourced and names it, so the sparkle no
            // longer needs to sit beside the one-liner below.
            CardEyebrowLabel(
                text = stringResource(id = R.string.indicators_alignments_read),
                color = LocalPulseColors.current.accentPrimary,
                iconRes = R.drawable.ic_ai_sparkle_filled,
                iconContentDescription = stringResource(id = R.string.summary_analysis_engine_content_description)
            )
            Spacer(modifier = Modifier.height(paddingMedium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SignalPill(
                    text = entry.agreement.name,
                    pillColor = entry.agreement.pillColor,
                    contentColor = entry.agreement.textColor,
                    outlined = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 💡 "STANCE" label -- unlike "ALIGNED"/"MIXED"/"DIVERGENT" on the left, the
                    // stance pill alone just reads GREEN/YELLOW/RED with no context for what that
                    // color is rating.
                    Text(
                        text = stringResource(id = R.string.indicators_stance_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(paddingSmall))
                    SignalPill(
                        text = entry.stance.name,
                        pillColor = entry.stance.pillColor,
                        contentColor = entry.stance.textColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(paddingMedium))
            // 💡 `oneLiner` is AI-authored prose (the model narrates around the code-computed
            // agreement/stance above it, never invents them); the eyebrow at the top of the card
            // is what marks it as AI-sourced.
            Text(
                text = entry.oneLiner,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
            // 💡 This card has no expand/collapse state -- `oneLiner` is always shown in full, so
            // the label just sits directly below it, always visible (was its own row above the
            // agreement/stance pills).
            Spacer(modifier = Modifier.height(paddingSmall))
            AiGeneratedLabel()
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val previewExecutive = DomainExecutiveBlock(
    headline = "Equities Maintain Upward Trajectory Amid Macroeconomic Divergence and Compressed Credit Spreads",
    alignmentWithMacro = AlignmentState.MARKET_AHEAD_OF_FUNDAMENTALS,
    alignmentNote = "A clear tension exists between the prevailing risk-on market regime and the underlying structural deterioration in macroeconomic indicators, though systemic risk remains well-contained.",
    whatChanged = "Today's baseline is being established and day-over-day comparisons will be available starting tomorrow.",
    shifts = listOf(
        DomainShift(metricId = "pe_ratio", direction = ShiftDirection.DETERIORATED, note = "Crossed into Expensive territory as prices outran trailing earnings."),
        DomainShift(metricId = "credit_spreads", direction = ShiftDirection.IMPROVED, note = "Tightened further into Healthy range, easing default-risk concerns.")
    )
)

private val previewScorecardEntry = DomainPillarScorecardEntry(
    pillar = IndicatorCategory.VALUATION,
    stance = SignalColor.RED,
    agreement = AgreementState.ALIGNED,
    oneLiner = "Valuation metrics are uniformly stretched across all primary gauges, showing tight alignment in their historical expensiveness."
)

private val previewValuationPillar = DomainIndicatorPillar(
    timestamp = 0L,
    masterGauge = null,
    metrics = listOf(
        DomainUnifiedMetric(
            id = "pe_ratio", name = "P/E Ratio (Trailing)", category = "VALUATION", subcategory = null,
            valueRaw = 25.79, valueDisplay = "25.79x", previousValueRaw = null, previousValueDisplay = null,
            changeRaw = 0.12, changeDisplay = "0.12%", signalText = "Expensive", signalColor = SignalColor.RED, releaseDate = null
        ),
        DomainUnifiedMetric(
            id = "pb_ratio", name = "Price-to-Book", category = "VALUATION", subcategory = null,
            valueRaw = 4.8, valueDisplay = "4.80x", previousValueRaw = null, previousValueDisplay = null,
            changeRaw = 0.02, changeDisplay = "0.02%", signalText = "Premium", signalColor = SignalColor.RED, releaseDate = null
        )
    )
)

private val previewMarketIndicators = MarketIndicators(
    dateId = "2026-08-22",
    lastSyncedTimestamp = System.currentTimeMillis(),
    aiSynthesis = DomainAiSynthesis(
        timestamp = System.currentTimeMillis(),
        contentFlags = emptyList(),
        executive = previewExecutive,
        pillarScorecard = listOf(previewScorecardEntry),
        horizons = DomainHorizons(null, null, null)
    ),
    tacticalMomentum = null,
    systemicRisk = null,
    valuation = previewValuationPillar,
    macroVitals = null
)

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewIndicatorsScreen() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        IndicatorsScreen(
            data = previewMarketIndicators,
            scaffoldPadding = PaddingValues(0.dp),
            selectedTabIndex = IndicatorsTab.VALUATION.ordinal,
            onTabSelected = {},
            favoriteMetricIds = setOf("pe_ratio"),
            onToggleFavoriteMetric = {},
            onNavigateToHorizons = {},
            onNavigateToMetricDetail = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewAiExecutiveBriefingHero() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            AiExecutiveBriefingHero(
                executive = previewExecutive,
                timestamp = System.currentTimeMillis(),
                metricNames = mapOf("pe_ratio" to "P/E Ratio (Trailing)", "credit_spreads" to "Credit Spreads (High Yield)")
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewHorizonNavigationCard() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            HorizonNavigationCard(onClick = {})
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewPillarSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            PillarSection(
                config = PillarUIConfig(
                    title = "Valuation",
                    pillarCategory = IndicatorCategory.VALUATION,
                    pillarData = previewValuationPillar
                ),
                scorecardEntry = previewScorecardEntry,
                onIndicatorClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewPillarScorecardCard() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            PillarScorecardCard(entry = previewScorecardEntry)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewShiftRow() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            ShiftRow(shift = previewExecutive.shifts.first(), metricName = "P/E Ratio (Trailing)")
        }
    }
}

