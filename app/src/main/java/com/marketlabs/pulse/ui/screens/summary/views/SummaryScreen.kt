package com.marketlabs.pulse.ui.screens.summary.views

import androidx.compose.animation.animateContentSize
import com.marketlabs.pulse.ui.components.CardExpandAnimationSpec
import com.marketlabs.pulse.ui.components.cardExpandPulse
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketDriver
import com.marketlabs.pulse.storage.model.summary.MarketPosition
import com.marketlabs.pulse.storage.model.summary.MarketSentiment
import com.marketlabs.pulse.storage.model.summary.MarketVerdict
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Positioning
import com.marketlabs.pulse.storage.model.summary.RiskItem
import com.marketlabs.pulse.storage.model.summary.Valuation
import com.marketlabs.pulse.storage.model.summary.WatchItem
import com.marketlabs.pulse.storage.model.summary.WhatsNewItem
import com.marketlabs.pulse.ui.components.AnalyzedAtHeader
import com.marketlabs.pulse.ui.components.DisclaimerFooter
import com.marketlabs.pulse.ui.components.ExpandCollapseAllToggle
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.PulseErrorState
import com.marketlabs.pulse.ui.components.PulseLoadingIndicator
import com.marketlabs.pulse.ui.components.PulseTabRow
import com.marketlabs.pulse.ui.components.rememberPerTabLazyListStates
import com.marketlabs.pulse.ui.components.bottomSheet.DriversInfoBottomSheet
import com.marketlabs.pulse.ui.components.bottomSheet.MarketGlossaryBottomSheet
import com.marketlabs.pulse.ui.components.widgets.CardEyebrowLabel
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.screens.stocks.detail.ViewMoreRow
import com.marketlabs.pulse.ui.screens.summary.DayContent
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.pillColor
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.Conviction
import com.marketlabs.pulse.utils.enums.DriverImpact
import com.marketlabs.pulse.utils.enums.IndicatorCategory
import com.marketlabs.pulse.utils.enums.MarketRegime
import com.marketlabs.pulse.utils.enums.RiskImpactLevel
import com.marketlabs.pulse.utils.enums.SignalColor
import com.marketlabs.pulse.utils.enums.SignalDirection
import com.marketlabs.pulse.utils.enums.TechnicalSetup
import com.marketlabs.pulse.utils.extensions.smartTitleCase
import com.marketlabs.pulse.utils.toRelativeDayLabel

/**
 * The main screen for displaying the Market Pulse report.
 *
 * Two things are pinned above the content: the calendar strip and the Pulse / Drivers / Outlook
 * tab row. The day is chosen only by tapping the strip -- horizontal swipes belong to the tab
 * pager, so a swipe never changes the day. Each tab is its own scrolling list for the selected day
 * (see [SummaryTabPage]); the relative date label and the "Analyzed as of" timestamp are the
 * first item of every tab, so they scroll away with the content.
 *
 * Changing day keeps the selected tab, scrolls each tab back to the top, and recreates
 * [SummaryExpansionState], so anything the reader had expanded collapses again and the Market
 * Movers / Macro Mix rows go back to following the screen-level expanded/collapsed default.
 *
 * @param contentByDateId What to render for each of [calendarDayIds] -- a loaded report, a
 * confirmed-empty past date, today-not-ready-yet, or a past date still syncing for the first
 * time. See [DayContent]. Always has one entry per [calendarDayIds] entry.
 * @param calendarDayIds The 7 NY dateIds behind the calendar strip, oldest first, last = today.
 * @param expandItemsByDefault Whether Market Movers and Macro Mix rows start expanded.
 * @param isPro Whether the user is entitled to the content a paid tier will lock.
 * @param onSectionExpand Fired once per tap that expands a hidden part (never on collapse), with
 * the section and the item's position within it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MarketSummaryScreen(
    contentByDateId: Map<String, DayContent>,
    calendarDayIds: List<String>,
    selectedDateId: String,
    onDateSelected: (String) -> Unit,
    scaffoldPadding: PaddingValues,
    onNavigateToIndicators: () -> Unit,
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    expandItemsByDefault: Boolean = false,
    onExpandItemsByDefaultChange: (Boolean) -> Unit = {},
    isPro: Boolean = true,
    onSectionExpand: (SummarySection, Int) -> Unit = { _, _ -> },
    // The jump from the Market Sentiment card to Posture -- Posture is a tab on the Insights
    // screen, not its own destination, same shape as onNavigateToIndicators above but landing on
    // a specific Insights tab.
    onNavigateToPosture: () -> Unit = {},
    onRetryDate: (String) -> Unit = {},
    // Pinned between the tab row and the pager; the Route fills it with the
    // enable-notifications banner, which renders nothing once answered. Empty by default so the
    // stateless screen (and its previews) needs no Hilt.
    banner: @Composable () -> Unit = {}
) {
    val data = (contentByDateId[selectedDateId] as? DayContent.Available)?.data
    val access = remember(isPro) { SummaryAccess(isPro) }

    // 💡 The most recent calendar day that actually HAS a report -- not necessarily today's own
    // dateId. Today's report typically doesn't post until the afternoon, so for a while every
    // morning "today" has no data yet; Position/whatChanged/whatsNew (the backend's live-only
    // composed fields, see the comment where they're gated below) should keep showing on
    // yesterday's page throughout that window rather than disappearing the instant the calendar
    // rolls over, then jump to today the moment today's report actually lands. `calendarDayIds` is
    // oldest-first, so the last Available entry scanning from the end is the one we want.
    val latestAvailableDateId =
        calendarDayIds.lastOrNull { contentByDateId[it] is DayContent.Available }

    // 💡 Which glossary sheet (if any) is open, and for which term -- regime/direction, setup,
    // and cycle zone each have their own tap target (a chevron on that one chip) rather than one
    // card-wide tap opening a combined sheet, so this needs to track *which* term was tapped, not
    // just open/closed. Resolves against `data` (the selected day's).
    var glossaryTarget by remember { mutableStateOf<GlossaryTarget?>(null) }
    var showDriversInfo by remember { mutableStateOf(false) }

    // Per-day: a new instance on every day change is what collapses everything again.
    val expansion = remember(selectedDateId) { SummaryExpansionState() }
    // A new default applies to every row, including ones the reader had tapped individually.
    LaunchedEffect(expansion, expandItemsByDefault) { expansion.clearRowOverrides() }

    val toggleSection: (SummarySection, Int) -> Unit = { section, position ->
        val allowed = section.feature?.let(access::allows) ?: true
        if (allowed && expansion.toggle(section, position, expandItemsByDefault)) {
            onSectionExpand(section, position)
        }
    }

    val pagerState = rememberPagerState(initialPage = selectedTabIndex) { SummaryTab.entries.size }

    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }

    // 💡 `settledPage`, not `currentPage` -- the same fix Indicators/Stock Detail use: keying off
    // `currentPage` pushes an intermediate, still-in-flight page value back to the ViewModel
    // mid-swipe/mid-animation, which the effect above then reads and uses to correct the pager,
    // fighting whatever gesture is still running.
    LaunchedEffect(pagerState.settledPage) {
        val page = pagerState.settledPage
        if (page != selectedTabIndex) {
            val locked = SummaryTab.entries[page].lockedBy?.let { !access.allows(it) } ?: false
            if (locked) pagerState.animateScrollToPage(selectedTabIndex) else onTabSelected(page)
        }
    }

    val listStates = rememberPerTabLazyListStates(SummaryTab.entries.size)
    // Saveable so a configuration change doesn't look like a day change and throw away the
    // restored scroll positions.
    var shownDateId by rememberSaveable { mutableStateOf(selectedDateId) }
    LaunchedEffect(selectedDateId) {
        if (shownDateId != selectedDateId) {
            shownDateId = selectedDateId
            listStates.forEach { it.scrollToItem(0) }
        }
    }

    val tabLabels = SummaryTab.entries.map { stringResource(id = it.labelRes) }

    // 💡 Top padding uses `scaffoldPadding`'s top component (the Scaffold's own measurement of
    // the top bar's real rendered height) instead of the raw status bar inset alone -- the raw
    // inset only accounts for the system status bar, not the app's own top bar sitting below it,
    // so content would start underneath the top bar rather than below it.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 💡 No horizontal padding here -- SummaryCalendarStrip applies its own internally (only
        // to the pill row, not its two dividers), so its dividers can span the full width
        // edge-to-edge while the pills stay inset. Tap-only: the strip has no pager to follow.
        SummaryCalendarStrip(
            dayIds = calendarDayIds,
            selectedDateId = selectedDateId,
            onDateSelected = onDateSelected,
            modifier = Modifier.padding(top = scaffoldPadding.calculateTopPadding())
        )

        PulseTabRow(
            tabs = tabLabels,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { index ->
                val locked = SummaryTab.entries[index].lockedBy?.let { !access.allows(it) } ?: false
                if (!locked) onTabSelected(index)
            },
            // Tracks the pager's drag finger-for-finger, so the highlight slides with the swipe.
            selectionPosition = { pagerState.currentPage + pagerState.currentPageOffsetFraction }
        )

        banner()

        // 💡 `weight(1f)`, not just `fillMaxSize()` -- required on every screen using this pager
        // pattern: guarantees the pager (the only scrollable, touchable content) always gets
        // whatever space is left under the pinned zone above, however tall that zone happens to
        // be.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val tab = SummaryTab.entries[page]
                SummaryTabPage(
                    tab = tab,
                    dateId = selectedDateId,
                    content = contentByDateId[selectedDateId] ?: DayContent.Loading,
                    isLatestWithReport = selectedDateId == latestAvailableDateId,
                    listState = listStates[page],
                    scaffoldPadding = scaffoldPadding,
                    expansion = expansion,
                    expandItemsByDefault = expandItemsByDefault,
                    showExpandSetting = access.allows(SummaryFeature.EXPAND_SETTING),
                    onExpandItemsByDefaultChange = onExpandItemsByDefaultChange,
                    onToggleSection = toggleSection,
                    onNavigateToIndicators = onNavigateToIndicators,
                    onNavigateToPosture = onNavigateToPosture,
                    onRegimeClick = { glossaryTarget = GlossaryTarget.REGIME },
                    onDirectionClick = { glossaryTarget = GlossaryTarget.DIRECTION },
                    onSetupClick = { glossaryTarget = GlossaryTarget.SETUP },
                    onCycleZoneClick = { glossaryTarget = GlossaryTarget.CYCLE_ZONE },
                    onDriversInfoClick = { showDriversInfo = true },
                    onRetryDate = onRetryDate
                )
            }
        }
    }

    // 💡 One sheet per tapped chip, not one combined sheet for the whole card -- `MarketVerdict`
    // has no `call` field (the backend removed it project-wide), so there's nothing that needs a
    // "verdict-wide" glossary. Regime and direction render as separate pills, so each gets its
    // own single-term sheet, matching every other chip on this screen (setup, cycle zone).
    when (glossaryTarget) {
        GlossaryTarget.REGIME -> {
            MarketGlossaryBottomSheet(
                currentRegime = data?.verdict?.regime?.label?.uppercase(),
                onDismiss = { glossaryTarget = null }
            )
        }

        GlossaryTarget.DIRECTION -> {
            MarketGlossaryBottomSheet(
                currentDirection = data?.verdict?.direction?.label?.uppercase(),
                onDismiss = { glossaryTarget = null }
            )
        }

        GlossaryTarget.SETUP -> {
            MarketGlossaryBottomSheet(
                currentSetup = data?.verdict?.setup?.label?.uppercase(),
                onDismiss = { glossaryTarget = null }
            )
        }

        GlossaryTarget.CYCLE_ZONE -> {
            MarketGlossaryBottomSheet(
                currentCycleZone = data?.position?.cycleZone,
                onDismiss = { glossaryTarget = null }
            )
        }

        null -> {}
    }

    if (showDriversInfo) {
        DriversInfoBottomSheet(onDismiss = { showDriversInfo = false })
    }
}

/**
 * One tab of [MarketSummaryScreen] for the selected day: a scrolling list holding the date header
 * followed by that tab's cards. Renders [content]: a loaded report's cards for [tab], or one of the
 * confirmed-empty/loading/not-ready states, each just a centered message.
 *
 * | Tab | Cards |
 * | --- | --- |
 * | Pulse | Market Signal (with its Market Read), Market Sentiment, Market Position, What's New |
 * | Drivers | Market Movers, Macro Mix |
 * | Outlook | Domino Effect, What to Watch, Risks to Market |
 *
 * @param isLatestWithReport Whether [dateId] is the most recent calendar day with an actual
 * report -- not necessarily today's own dateId (see [MarketSummaryScreen]'s `latestAvailableDateId`
 * for why). Gates the live-only Position/whatChanged/whatsNew sections.
 */
@Composable
private fun SummaryTabPage(
    tab: SummaryTab,
    dateId: String,
    content: DayContent,
    isLatestWithReport: Boolean,
    listState: LazyListState,
    scaffoldPadding: PaddingValues,
    expansion: SummaryExpansionState,
    expandItemsByDefault: Boolean,
    showExpandSetting: Boolean,
    onExpandItemsByDefaultChange: (Boolean) -> Unit,
    onToggleSection: (SummarySection, Int) -> Unit,
    onNavigateToIndicators: () -> Unit,
    onNavigateToPosture: () -> Unit,
    onRegimeClick: () -> Unit,
    onDirectionClick: () -> Unit,
    onSetupClick: () -> Unit,
    onCycleZoneClick: () -> Unit,
    onDriversInfoClick: () -> Unit,
    onRetryDate: (String) -> Unit = {}
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    // `key(dateId)` rather than relying on `remember` inside the cards: this list's slots are the
    // same from one day to the next, so without it a card's own local state (the pulse animation,
    // for one) would carry over onto the new day's content.
    key(dateId) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                top = paddingLarge,
                bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge,
                start = paddingLarge,
                end = paddingLarge
            ),
            verticalArrangement = Arrangement.spacedBy(paddingLarge)
        ) {
            // 💡 Each tab renders the selected day's own label (and timestamp, once loaded) as its
            // first item, scrolling away with the rest of that tab rather than sitting in the
            // pinned zone. The expanded/collapsed default sits on the same row, but only on Drivers,
            // the one tab whose rows it controls.
            item {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dateId.toRelativeDayLabel(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_micro)))
                        if (content is DayContent.Available) {
                            AnalyzedAtHeader(timestamp = content.data.lastUpdated)
                        }
                    }
                    if (content is DayContent.Available && showExpandSetting && tab == SummaryTab.DRIVERS) {
                        ExpandCollapseAllToggle(
                            expanded = expandItemsByDefault,
                            onToggle = { onExpandItemsByDefaultChange(!expandItemsByDefault) }
                        )
                    }
                }
            }

            when (content) {
                is DayContent.NotAvailable -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.summary_not_available_for_date),
                                style = MaterialTheme.typography.bodyMedium,
                                color = LocalPulseColors.current.onSurfaceMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                DayContent.TodayNotReady -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.summary_today_not_ready),
                                style = MaterialTheme.typography.bodyMedium,
                                color = LocalPulseColors.current.onSurfaceMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                DayContent.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            PulseLoadingIndicator()
                        }
                    }
                }

                is DayContent.Error -> {
                    item {
                        PulseErrorState(
                            message = content.message,
                            onRetry = { onRetryDate(content.dateId) },
                            modifier = Modifier.fillParentMaxSize()
                        )
                    }
                }

                is DayContent.Available -> {
                    val validData = content.data
                    var hasCards = false

                    when (tab) {
                        SummaryTab.PULSE -> {
                            // 💡 Signal's Market Read expands inside the card; there is no
                            // separate Read card or sheet. Navigating to Posture is a link at the
                            // bottom of Market Sentiment, not a whole-card tap, so the two
                            // interactions don't compete.
                            validData.verdict?.let { verdict ->
                                hasCards = true
                                item {
                                    SignalSection(
                                        verdict = verdict,
                                        onRegimeClick = onRegimeClick,
                                        onDirectionClick = onDirectionClick,
                                        readExpanded = expansion.isExpanded(
                                            SummarySection.MARKET_READ, 0, expandItemsByDefault
                                        ),
                                        onReadToggle = { onToggleSection(SummarySection.MARKET_READ, 0) }
                                    )
                                }
                            }

                            validData.marketSentiment?.let { sentiment ->
                                hasCards = true
                                item {
                                    MarketSentimentCard(
                                        sentiment = sentiment,
                                        onClick = onNavigateToPosture,
                                        isExpanded = expansion.isExpanded(
                                            SummarySection.MARKET_SENTIMENT, 0, expandItemsByDefault
                                        ),
                                        onToggleExpanded = { onToggleSection(SummarySection.MARKET_SENTIMENT, 0) }
                                    )
                                }
                            }

                            // Commented out until further action -- the drivers list read as an
                            // ambiguous signal in practice rather than a useful one. DriversSection
                            // and its plumbing are left in place in case this gets revisited.
                            // val drivers = validData.drivers
                            // if (!drivers.isNullOrEmpty()) {
                            //     item {
                            //         DriversSection(
                            //             drivers = drivers,
                            //             onClick = onNavigateToIndicators,
                            //             onInfoClick = onDriversInfoClick
                            //         )
                            //     }
                            // }

                            // 💡 Position/whatChanged/whatsNew are composed by the backend at
                            // request time regardless of which dateId was requested -- always the
                            // CURRENT live values, never historically accurate to a past date's
                            // own report. Gated to whichever calendar day currently has the most
                            // recent actual report (isLatestWithReport), not strictly "today" --
                            // today's report usually doesn't post until the afternoon, so these
                            // sections should keep showing on yesterday's page through that window
                            // rather than vanishing the moment the calendar rolls over, then jump
                            // onto today's page the moment today's report actually lands.
                            if (isLatestWithReport) {
                                validData.position?.let { position ->
                                    hasCards = true
                                    item {
                                        MarketPositionSection(
                                            position = position,
                                            setup = validData.verdict?.setup,
                                            whatChanged = validData.whatChanged,
                                            onSetupClick = onSetupClick,
                                            onCycleZoneClick = onCycleZoneClick,
                                            onShowIndicatorsClick = onNavigateToIndicators,
                                            whatChangedExpanded = expansion.isExpanded(
                                                SummarySection.WHAT_CHANGED, 0, expandItemsByDefault
                                            ),
                                            onWhatChangedToggle = { onToggleSection(SummarySection.WHAT_CHANGED, 0) }
                                        )
                                    }
                                }

                                val whatsNew = validData.whatsNew
                                if (!whatsNew.isNullOrEmpty()) {
                                    hasCards = true
                                    item { WhatsNewSection(whatsNew) }
                                }
                            }
                        }

                        SummaryTab.DRIVERS -> {
                            val stories = validData.leadStories
                            if (!stories.isNullOrEmpty()) {
                                hasCards = true
                                item {
                                    LeadStoriesSection(
                                        stories = stories,
                                        isItemExpanded = { index ->
                                            expansion.isExpanded(SummarySection.MARKET_MOVERS, index, expandItemsByDefault)
                                        },
                                        onToggleItem = { index -> onToggleSection(SummarySection.MARKET_MOVERS, index) }
                                    )
                                }
                            }

                            val macros = validData.macroMix
                            if (!macros.isNullOrEmpty()) {
                                hasCards = true
                                item {
                                    MacroMixSection(
                                        macros = macros,
                                        isItemExpanded = { index ->
                                            expansion.isExpanded(SummarySection.MACRO_MIX, index, expandItemsByDefault)
                                        },
                                        onToggleItem = { index -> onToggleSection(SummarySection.MACRO_MIX, index) }
                                    )
                                }
                            }
                        }

                        SummaryTab.OUTLOOK -> {
                            validData.dominoEffect?.let { domino ->
                                hasCards = true
                                item { DominoCard(domino) }
                            }

                            val watch = validData.watch
                            if (!watch.isNullOrEmpty()) {
                                hasCards = true
                                item { WatchSection(watch) }
                            }

                            val risks = validData.risks
                            if (!risks.isNullOrEmpty()) {
                                hasCards = true
                                item { RisksSection(risks) }
                            }
                        }
                    }

                    if (!hasCards) {
                        item {
                            Text(
                                text = stringResource(id = R.string.summary_tab_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = LocalPulseColors.current.onSurfaceMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item { DisclaimerFooter(showAiDisclosure = true) }
                }
            }
        }
    }
}

private enum class GlossaryTarget { REGIME, DIRECTION, SETUP, CYCLE_ZONE }

// ---------------------------------------------------------
// COMPONENT LIBRARY
// ---------------------------------------------------------

/**
 * The top-of-screen Signal card and the closing "The Read" card both read from the same
 * [MarketVerdict] (the backend delivers signal + the_read as one object). This one renders the
 * glanceable top flash: `regime` (the model's 6-value classification of the market's current
 * phase) and `direction` (RISK_ON/RISK_OFF/MIXED, the code-derived aggregate read) render as two
 * separate pills side by side, each its own tap target with a trailing chevron opening a
 * glossary sheet scoped to just that term -- so direction's own value reads as text, not only as
 * a tint. `signal_line` (the flash headline) is the largest prose on the card; tapping the card
 * expands the fuller analysis + posture read ([MarketReadContent]) inside it, for a reader who
 * wants the "why". `conviction` renders as a label/level row over a meter, always inline, never
 * behind a tap -- this is the field that makes the verdict earned confidence instead of a
 * black-box "trust me." `setup` lives on the Market
 * Position card.
 */
@Composable
fun SignalSection(
    verdict: MarketVerdict,
    onRegimeClick: () -> Unit,
    onDirectionClick: () -> Unit,
    readExpanded: Boolean = false,
    onReadToggle: () -> Unit = {}
) {
    val pulseColors = LocalPulseColors.current
    val directionColor = verdict.direction.toSignalColor()
    // Nothing to reveal when the backend sent neither -- then the card is not tappable at all.
    val hasRead = verdict.analysis != null || verdict.posture != null

    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth().cardExpandPulse(readExpanded),
        onClick = if (hasRead) onReadToggle else null
    ) {
        Column(modifier = Modifier.animateContentSize(CardExpandAnimationSpec)) {
            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                CardEyebrowLabel(
                    text = stringResource(id = R.string.section_signal),
                    color = MaterialTheme.colorScheme.primary
                )

                if (verdict.regime != null || verdict.direction != null) {
                    Spacer(modifier = Modifier.height(paddingMedium))
                    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {
                        // 💡 Outlined, not signal-colored -- regime is a classification (which of
                        // 6 phases the model thinks we're in), not a bullish/bearish read on its
                        // own; the direction pill next to it is where the color/arrow read lives.
                        verdict.regime?.let {
                            SignalPill(
                                text = it.label,
                                pillColor = Color.Transparent,
                                contentColor = pulseColors.onSurfaceMuted,
                                outlined = true,
                                trailingIcon = { GlossaryChevron(tint = pulseColors.onSurfaceMuted) },
                                onClick = onRegimeClick
                            )
                        }
                        verdict.direction?.let {
                            SignalPill(
                                text = it.label,
                                pillColor = directionColor.pillColor,
                                contentColor = directionColor.textColor,
                                trailingIcon = { GlossaryChevron(tint = directionColor.textColor) },
                                onClick = onDirectionClick
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(paddingMedium))

                // 💡 The flash -- largest prose on the card, no callout box around it; a headline
                // doesn't need to be quoted. titleMedium/bold. The trailing arrow (same up/down
                // arrow Market Sentiment uses) marks the card as tap-to-expand: the Market Read
                // opens below the conviction meter, inside this same card.
                verdict.signalLine?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = it.smartTitleCase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (hasRead) {
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                            Icon(
                                painter = painterResource(id = if (readExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large))
                            )
                        }
                    }
                }
            }

            if (readExpanded && hasRead) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = dimensionResource(id = R.dimen.border_thin)
                )
                MarketReadContent(verdict = verdict)
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )

            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                if (verdict.conviction != null) {
                    // 💡 Last in the card: collapsed it follows the headline directly; expanded it
                    // closes the card after the Market Read and Where Capital's Moving blocks.
                    // Label and level share one row (level pinned to the card's far edge), with
                    // the bar underneath -- a full-width continuous fill reads strength at a glance
                    // better than discrete segments would. The level word is the whole explanation;
                    // the backend's "N of 4 signals aligned" sentence is deliberately not shown.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.label_conviction),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = pulseColors.onSurfaceMuted,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = verdict.conviction.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = directionColor.textColor
                        )
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                    // 💡 .textColor, not .pillColor -- pillColor is tuned to be a soft background a
                    // bolder text color sits on top of, so it read as a near-invisible fill when
                    // used as the filled color of a bar itself. .textColor is the more saturated
                    // half of the pair, tuned to already read clearly as a foreground color.
                    ConvictionMeter(
                        conviction = verdict.conviction,
                        filledColor = directionColor.textColor
                    )
                }
            }
        }
    }
}

private fun SignalDirection?.toSignalColor(): SignalColor = when (this) {
    SignalDirection.RISK_ON -> SignalColor.GREEN
    SignalDirection.RISK_OFF -> SignalColor.RED
    SignalDirection.MIXED -> SignalColor.YELLOW
    SignalDirection.UNKNOWN, null -> SignalColor.UNKNOWN
}

// 💡 The backend doesn't send a color for `setup` the way it does for driver direction or
// market_position's signal_color -- setup is read contrarian throughout this app (see
// assets/market_glossary.json's "setups" definitions: EXHAUSTED OVERSOLD/OVERSOLD read as buying
// opportunities, OVERBOUGHT/BLOW-OFF TOP as danger), so the color has to be derived client-side from that same
// contrarian reading, not a literal "up = green" mapping. `regime` gets no equivalent mapping at
// all -- its pill on the Signal card is outlined/neutral, not signal-colored (see SignalSection's
// doc comment).
private fun TechnicalSetup?.toSignalColor(): SignalColor = when (this) {
    TechnicalSetup.EXHAUSTED_OVERSOLD, TechnicalSetup.OVERSOLD -> SignalColor.GREEN
    TechnicalSetup.NEUTRAL_MEAN -> SignalColor.YELLOW
    TechnicalSetup.OVERBOUGHT, TechnicalSetup.BLOW_OFF_TOP, TechnicalSetup.BEARISH_DIVERGENCE -> SignalColor.RED
    TechnicalSetup.UNKNOWN, null -> SignalColor.UNKNOWN
}

// cycle_zone is a free-text field (system/market_regime's risk_on/risk_off/neutral, relabeled --
// see marketPulseComposer.ts's deriveCycleZone), always one of exactly these three phrases or
// null, matched by substring rather than exact equality so a future label tweak (e.g. added
// punctuation) degrades to UNKNOWN instead of breaking silently.
private fun String?.cycleZoneToSignalColor(): SignalColor = when {
    this == null -> SignalColor.UNKNOWN
    this.contains("EXPANSION", ignoreCase = true) -> SignalColor.GREEN
    this.contains("CONTRACTION", ignoreCase = true) -> SignalColor.RED
    this.contains("TRANSITION", ignoreCase = true) -> SignalColor.YELLOW
    else -> SignalColor.UNKNOWN
}

/**
 * A full-width continuous bar, filled left-to-right by [conviction] (LOW = 1/3, MODERATE = 2/3,
 * HIGH = full) -- same shape as CardStyleShowcase.kt's SYNTHESIS "headline + divider + meter"
 * sample: a continuous fill reads relative strength at a glance the way a real progress/strength
 * meter would, rather than blocky discrete steps.
 */
@Composable
private fun ConvictionMeter(conviction: Conviction, filledColor: Color) {
    var filledFractionTarget by remember { mutableFloatStateOf(0f) }
    val filledFraction by animateFloatAsState(
        targetValue = filledFractionTarget,
        animationSpec = tween(durationMillis = 1500),
        label = "conviction_meter_fraction"
    )
    LaunchedEffect(conviction) {
        filledFractionTarget = when (conviction) {
            Conviction.LOW -> 1f / 3f
            Conviction.MODERATE -> 2f / 3f
            Conviction.HIGH -> 1f
            Conviction.UNKNOWN -> 0f
        }
    }
    val barHeight = dimensionResource(id = R.dimen.padding_medium)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(
                // 💡 A translucent `onSurface` overlay, not `colorScheme.surfaceVariant` -- this
                // card's own SYNTHESIS background is `accentSurfaceStrong`, an accent-tinted
                // surface close enough in value to `surfaceVariant` in dark mode that the track
                // would read as invisible, indistinguishable from the card behind it. A translucent
                // overlay of the foreground color instead guarantees contrast against whatever
                // background sits behind it.
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill))
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(filledFraction)
                .height(barHeight)
                .background(
                    color = filledColor,
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill))
                )
        )
    }
}

/**
 * The drivers[] chip row -- same `FlowRow` + `SignalPill` pattern `ConditionChipRow.kt` (the
 * stock domain's condition-chip row) uses, one step cleaner here since `MarketDriver.direction`
 * is already a real `SignalColor` from the mapper (never a raw string needing a hand-rolled
 * `when`). Sorted HIGH-impact first, same "most-important-first" sort `WatchList.kt` uses for its
 * own urgency field.
 *
 * 💡 No ▲/▼/▪ glyph on the pill -- an arrow reads as "the data itself went up/down," which isn't
 * what `direction` means (it's the model's reconciled effect-on-equities call, not a mechanical
 * copy of the indicator's own reading; see [DriversInfoBottomSheet]). The color alone carries that
 * signal; a glyph would add a second, misleading claim on top of it.
 *
 * The header (title + info icon) lives inside the card, same header+divider treatment as
 * [LeadStoriesSection]/[MacroMixSection]. The info icon stays its own separate tap target
 * (opens [com.marketlabs.pulse.ui.components.bottomSheet.DriversInfoBottomSheet]) -- a nested
 * `clickable` consumes its own taps
 * before they reach the outer card's `onClick`, same as `DataCardTitleWithInfo`'s icon does inside
 * its own row elsewhere in the app. The whole card (below the header) is still the tap target into
 * Indicators (using the same tab-preserving `popUpTo`/`launchSingleTop`/`restoreState` pattern the
 * bottom nav bar itself uses -- see `MainActivity.kt`'s `FloatingBottomNav` `onItemClick`).
 * `drivers[].direction` means "this driver's net effect on equities," not "the underlying
 * indicator's own reading" -- a distinction a color/arrow alone
 * can't communicate, which is what the info icon explains.
 */
@Composable
fun DriversSection(drivers: List<MarketDriver>, onClick: () -> Unit, onInfoClick: () -> Unit) {
    val sorted = drivers.sortedBy { if (it.impact == DriverImpact.HIGH) 0 else 1 }

    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_large)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.section_drivers).uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = stringResource(id = R.string.drivers_info_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.icon_size_small))
                        .clickable(onClick = onInfoClick)
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            // 💡 The pills row isn't the tap target -- navigation lives on its own ViewMoreRow
            // below (no bare trailing chevron implying the whole row is tappable).
            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
                ) {
                    sorted.forEach { driver ->
                        SignalPill(
                            text = driver.label ?: "",
                            pillColor = driver.direction.pillColor,
                            contentColor = driver.direction.textColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                ViewMoreRow(
                    text = stringResource(id = R.string.summary_show_indicators),
                    onClick = onClick
                )
            }
        }
    }
}

/**
 * market_position -- a Reader-side join, already merged into the same response as everything
 * else (not a second network call). Every sub-block can be null on cold start, so each one
 * renders independently. `signal_text`/`signal_color` are pre-classified backend-side; rendered
 * directly, no client threshold logic.
 */
@Composable
fun MarketPositionSection(
    position: MarketPosition,
    setup: TechnicalSetup?,
    whatChanged: String?,
    onSetupClick: () -> Unit,
    onCycleZoneClick: () -> Unit,
    onShowIndicatorsClick: () -> Unit,
    // 💡 Collapsed to 3 lines by default, tap to read the rest -- the app-wide default for
    // AI-synthesis body text. What Changed can run several sentences and this card already
    // carries the position gauges/pills above it, so an uncollapsed paragraph pushed everything
    // below it further down the page than its own importance justified. Hoisted so the screen can
    // reset it when the day changes.
    whatChangedExpanded: Boolean = false,
    onWhatChangedToggle: () -> Unit = {}
) {
    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(id = R.string.section_market_position).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                // 💡 setup lands here now, not on the Signal card or The Read -- it's a technical
                // read on where price sits (oversold/overbought/...), which fits this card's own
                // subject better than the verdict cards' regime/conviction focus. Both chips are
                // their own tap target (trailing chevron), each opening a glossary scoped to just
                // that term -- same pattern the Signal card's regime chip uses.
                if (position.cycleZone != null || setup != null) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
                    ) {
                        position.cycleZone?.let {
                            val color = it.cycleZoneToSignalColor()
                            SignalPill(
                                text = it,
                                pillColor = color.pillColor,
                                contentColor = color.textColor,
                                trailingIcon = { GlossaryChevron(tint = color.textColor) },
                                onClick = onCycleZoneClick
                            )
                        }
                        setup?.let {
                            val color = it.toSignalColor()
                            SignalPill(
                                text = it.label,
                                pillColor = color.pillColor,
                                contentColor = color.textColor,
                                trailingIcon = { GlossaryChevron(tint = color.textColor) },
                                onClick = onSetupClick
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard)))
                }

                position.positioning?.let { positioning ->
                    PositioningGauge(
                        rangePosition = positioning.rangePosition,
                        windowLabel = positioning.windowLabel,
                        signalText = positioning.signalText,
                        signalColor = positioning.signalColor
                    )

                    val windowLabel =
                        positioning.windowLabel
                            ?: stringResource(id = R.string.label_window_fallback)
                    val pctText = listOfNotNull(
                        positioning.pctFrom52wHigh?.let {
                            stringResource(
                                id = R.string.label_pct_from_high,
                                it,
                                windowLabel
                            )
                        },
                        positioning.pctFrom52wLow?.let {
                            stringResource(
                                id = R.string.label_pct_from_low,
                                it,
                                windowLabel
                            )
                        }
                    ).joinToString(",\n")
                    if (pctText.isNotBlank()) {
                        Text(
                            text = pctText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium))
                        )
                    }
                }

            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )

            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {

                // 💡 horizons (SHORT/MEDIUM/LONG risk_level + key_driver) deliberately not rendered
                // here -- staying in the domain model/cache, just not surfaced on this screen; it's
                // landing on the Indicators tab instead once that work starts.

                position.valuation?.pePercentile5y?.let {
                    Text(
                        text = "${stringResource(id = R.string.label_valuation)} $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
                    )
                }

                // 💡 Its own eyebrow-labeled block now -- same format Market Sentiment/Market
                // Read/Where Capital's Moving use (CardEyebrowLabel + the content below it) --
                // rather than an inline "What Changed: ..." sentence tacked onto the footer.
                whatChanged?.let {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onWhatChangedToggle)
                    ) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CardEyebrowLabel(
                                text = stringResource(id = R.string.label_what_changed),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                painter = painterResource(id = if (whatChangedExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large))
                            )
                        }
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = if (whatChangedExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.animateContentSize(CardExpandAnimationSpec)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                ViewMoreRow(
                    text = stringResource(id = R.string.summary_show_indicators),
                    onClick = onShowIndicatorsClick
                )
            }
        }
    }
}

/**
 * whats_new[] -- a deterministic (non-AI), most-recent-first list of indicators
 * whose data posted in the last 7 days. Styled as one `DATA`-style card holding every row, split
 * by a divider per entry -- same "one card, divided rows" structure [MarketReadContent]'s
 * analysis/posture split uses, one step further since the row count here is dynamic.
 * Each row is two columns: the indicator's name + its `signalText` read (e.g. "Healthy") on the
 * left, tinted by `signalColor` -- the same pre-classified backend read [MarketPositionSection]'s
 * gauge caption and [RisksSection]'s severity pill already render directly with no client-side
 * threshold logic; `valueDisplay`/`previousValueDisplay` end-aligned on the right, labeled
 * "Current"/"Previous". `previousValueDisplay` isn't sent by the backend yet -- the Previous line
 * just doesn't render until it ships, same null-safe `?.let` pattern as every other
 * optional field here. `releaseDate` and `category` are deliberately not shown -- both kept on the
 * domain model for whichever future screen wants them, but not part of this card's layout.
 * TODO(hierarchy-placement): this section's placement (currently right after Market Position) is
 * still a reasonable-default guess, not a confirmed design decision.
 */
@Composable
fun WhatsNewSection(items: List<WhatsNewItem>) {
    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(id = R.string.section_whats_new).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            items.forEachIndexed { index, item ->
                val signalTextColor = item.signalColor.textColor

                Row(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.label ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        item.signalText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = signalTextColor,
                                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_micro))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))

                    Column(horizontalAlignment = Alignment.End) {
                        item.valueDisplay?.let {
                            Text(
                                text = stringResource(id = R.string.label_whats_new_current, it),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End
                            )
                        }
                        item.previousValueDisplay?.let {
                            Text(
                                text = stringResource(id = R.string.label_whats_new_previous, it),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_micro))
                            )
                        }
                    }
                }

                if (index != items.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
                }
            }
        }
    }
}

/**
 * The watch[] list -- one `DATA` style card (same header+divider treatment as
 * [LeadStoriesSection]/[MacroMixSection]) holding every item, timeframe using the same [TagPill]
 * [MacroMixSection]'s tag does, on its own line below the label rather than sharing a row with it
 * (so a long label never crowds it). A full-width divider sits between entries; no divider within
 * one entry's own label/why text.
 */
@Composable
fun WatchSection(watch: List<WatchItem>) {
    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(id = R.string.section_watch).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            watch.forEachIndexed { index, item ->
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                    Text(
                        text = item.label ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    item.timeframe?.let {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                        TagPill(text = it)
                    }
                    item.why?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium))
                        )
                    }
                }
                if (index != watch.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
                }
            }
        }
    }
}

/**
 * The risks[] list -- one `DATA` style card (same header+divider treatment as
 * [LeadStoriesSection]/[WatchSection]) holding every item, severity colored through the same
 * [RiskImpactLevel] color extension [HorizonChip] uses (`ui/theme/SignalColorExtensions.kt`), not
 * a hand-rolled `when` here, on its own line below the risk text rather than sharing a row with it
 * (so a long risk description never crowds it). A full-width divider sits between entries; no
 * divider within one entry's own risk/note text.
 */
@Composable
fun RisksSection(risks: List<RiskItem>) {
    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(id = R.string.section_risks).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            risks.forEachIndexed { index, item ->
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                    Text(
                        text = item.risk ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    item.severity?.let {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                        SignalPill(
                            text = it.label.uppercase(),
                            pillColor = it.pillColor,
                            contentColor = it.textColor
                        )
                    }
                    item.note?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium))
                        )
                    }
                }
                if (index != risks.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
                }
            }
        }
    }
}

/**
 * The Market Read, shown inside the Market Signal card once it is expanded:
 * [MarketVerdict.analysis] is the verdict_text field, [MarketVerdict.posture] the renamed-from-
 * action field. posture is descriptive market-stance text, never an instruction -- rendered as a
 * small labeled observation under its own divider, never under an "action plan"-style header or
 * its own card.
 *
 * Both blocks are separately-padded Columns with the divider between them at this outer level, so
 * the divider spans the card's full width instead of stopping at the content inset.
 */
@Composable
private fun MarketReadContent(verdict: MarketVerdict) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    Column {
        // 💡 No regime/setup pills here -- regime is already the card's own chip and repeating it
        // would be redundant; setup lives on Market Position.
        verdict.analysis?.let { analysis ->
            Column(modifier = Modifier.padding(paddingLarge)) {
                CardEyebrowLabel(
                    text = stringResource(id = R.string.market_read),
                    color = MaterialTheme.colorScheme.primary
                )
                // Same eyebrow-to-content gap Market Sentiment uses (padding_medium).
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                Text(
                    text = analysis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 💡 posture rendered as its own distinct, scannable "positioning" beat -- separated
        // from verdict_text by the divider rather than tacked on as another paragraph in the
        // same block, exactly so it doesn't read as a third restatement of the same thesis.
        verdict.posture?.let { posture ->
            if (verdict.analysis != null) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = dimensionResource(id = R.dimen.border_thin)
                )
            }
            Column(modifier = Modifier.padding(paddingLarge)) {
                CardEyebrowLabel(
                    text = stringResource(id = R.string.label_posture),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                Text(
                    text = posture,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 💡 The section title lives INSIDE the card as its own small-caps accent header with a
 * full-bleed divider directly beneath it (see CardStyleShowcase.kt's `DataTimelineSample`
 * pattern), not as a separate `SectionTitle` list item above N individual story cards. All
 * stories live in ONE `PulseCard`, separated by an inset divider between entries -- only the
 * boundary between two different stories gets one, not the gap between one story's own headline
 * and summary.
 *
 * @param stories The lead-story items to display. Entries with no headline are skipped; if none
 *   have a headline, the whole section renders nothing.
 */
@Composable
fun LeadStoriesSection(
    stories: List<NewsItem>,
    isItemExpanded: (Int) -> Boolean = { false },
    onToggleItem: (Int) -> Unit = {}
) {
    // Positions index the original list, not the filtered one, so a section/position pair stays
    // stable if an entry without a headline is skipped.
    val validStories = stories.withIndex().filter { it.value.headline != null }
    if (validStories.isEmpty()) return

    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(id = R.string.section_lead_stories).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            // 💡 Each story gets its OWN padded Column, with an unpadded (full-card-width)
            // divider between them at this outer level -- same "divider spans the full width
            // between two separately-padded Columns" idiom SignalSection/MacroMixSection use, rather
            // than one padded Column wrapping every story (which would inset the divider to stop
            // short at the content margin instead of reaching the card's edges).
            validStories.forEachIndexed { rowIndex, (position, story) ->
                val expanded = isItemExpanded(position)
                val summary = story.summary
                // 💡 Collapsed is the headline alone, no preview lines -- the summary is the part a
                // paid tier can lock, so hiding it must not change the row's shape.
                Column(
                    modifier = Modifier
                        .animateContentSize(CardExpandAnimationSpec)
                        .then(if (summary != null) Modifier.clickable { onToggleItem(position) } else Modifier)
                        .padding(dimensionResource(id = R.dimen.padding_large))
                ) {
                    ExpandableRowHeader(
                        expanded = expanded,
                        expandable = summary != null
                    ) {
                        Text(
                            text = story.headline!!,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (expanded && summary != null) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (rowIndex != validStories.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
                }
            }
        }
    }
}

/**
 * The Market Sentiment card -- AI-authored cohort-positioning synthesis (headline + summary),
 * styled SYNTHESIS like Signal/The Read (the two other AI-narrative cards on this screen).
 * Collapsed by default to a 3-line clamp of `summary`
 * below the headline (tapping the card toggles [isExpanded], same up/down-arrow-beside-the-headline
 * pattern Indicators' Today's Read card uses) -- the app-wide default for AI-synthesis body text.
 * Navigating to Posture is its own ViewMoreRow at the bottom, not a whole-card tap, so the two
 * interactions (expand-in-place vs. leave-the-screen) don't compete on the same tap target.
 * When there's no headline (rare -- the caller only omits this card entirely when both
 * headline and summary are blank), there's nothing meaningful to collapse *to*, so the summary
 * just always renders unclamped instead of hiding behind a headline-less collapsed state.
 *
 * @param sentiment The [MarketSentiment] to display. Caller (`MarketSummaryScreen`) already omits
 * this card entirely when both headline and summary are blank; either field alone still renders.
 * @param isExpanded Owned by the screen, which collapses it again when the day changes.
 */
@Composable
fun MarketSentimentCard(
    sentiment: MarketSentiment,
    onClick: () -> Unit,
    isExpanded: Boolean = false,
    onToggleExpanded: () -> Unit = {}
) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val headline = sentiment.headline

    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth().cardExpandPulse(isExpanded),
        onClick = onToggleExpanded
    ) {
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
                .animateContentSize(CardExpandAnimationSpec)
        ) {
            // 💡 Header lives inside the card, same as SignalSection's "Market Signal" -- not
            // a separate SectionTitle list item. No per-card `AiGeneratedLabel` on Summary --
            // nearly every card on this screen is AI-authored, so the disclosure lives once in
            // this screen's own footer instead of repeating on each card (see `DisclaimerFooter`
            // usage in this file).
            CardEyebrowLabel(
                text = stringResource(id = R.string.section_market_sentiment),
                color = MaterialTheme.colorScheme.primary
            )

            if (headline != null) {
                Spacer(modifier = Modifier.height(paddingMedium))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = headline.smartTitleCase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                    Icon(
                        painter = painterResource(id = if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large))
                    )
                }
            }

            sentiment.summary?.let { summary ->
                Spacer(modifier = Modifier.height(paddingMedium))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (headline == null || isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(paddingMedium))
            ViewMoreRow(
                text = stringResource(id = R.string.summary_show_posture_positioning),
                onClick = onClick
            )
        }
    }
}

/**
 * The macro_mix[] list -- one `DATA` style card (same header+divider treatment as
 * [LeadStoriesSection]/[WatchSection]) holding every macro-economic factor. No left-rail accent
 * bar -- plain padded entries, matching every other merged-card section; a full-width divider
 * sits between entries. `tag` renders on its own line below the heading rather than sharing a row
 * with it, so a long headline never crowds it. There's no divider between one item's own
 * headline/tag row and its summary, matching [LeadStoriesSection]'s "only the boundary between
 * two different entries is worth marking" reasoning.
 *
 * @param macros The macro items to display. Entries with no headline are skipped; if none have a
 *   headline, the whole section renders nothing.
 */
@Composable
fun MacroMixSection(
    macros: List<MacroItem>,
    isItemExpanded: (Int) -> Boolean = { false },
    onToggleItem: (Int) -> Unit = {}
) {
    val validMacros = macros.withIndex().filter { it.value.headline != null }
    if (validMacros.isEmpty()) return

    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(id = R.string.section_macro_mix).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            validMacros.forEachIndexed { rowIndex, (position, item) ->
                val expanded = isItemExpanded(position)
                val summary = item.summary
                // 💡 No left-rail accent bar -- plain padded Column, same as every other
                // merged-card entry (Lead Stories/Watch/Risks). The tag sits on its own line below
                // the heading rather than sharing a row with it, so a long headline never has to
                // compete with the tag for width or push it to a cramped corner.
                Column(
                    modifier = Modifier
                        .animateContentSize(CardExpandAnimationSpec)
                        .then(if (summary != null) Modifier.clickable { onToggleItem(position) } else Modifier)
                        .padding(dimensionResource(id = R.dimen.padding_large))
                ) {
                    ExpandableRowHeader(
                        expanded = expanded,
                        expandable = summary != null
                    ) {
                        Text(
                            text = item.headline!!,
                            style = MaterialTheme.typography.titleMedium,
                            // 💡 Card titles are always onSurface -- see LeadStoriesSection above.
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // The tag stays visible while collapsed: with the headline it is what the
                    // reader scans, and it is not part of the summary a paid tier will lock.
                    item.tag?.let { tag ->
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                        TagPill(text = tag.label)
                    }
                    if (expanded && summary != null) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (rowIndex != validMacros.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
                }
            }
        }
    }
}

/**
 * Displays the "Domino Effect" card, showing a causal relationship.
 *
 * Layout:
 * - Trigger: The event that started it.
 * - Impact: The result of that event.
 *
 * @param domino The [DominoEffect] object. Requires both 'trigger' and 'impact' to render.
 */
@Composable
fun DominoCard(domino: DominoEffect) {
    val trigger = domino.trigger ?: return
    val impact = domino.impact ?: return
    val outlook = domino.outlook ?: return

    // 💡 DATA style -- see LeadStoriesSection above.
    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.section_domino_effect).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                DominoTimelineStep(
                    title = stringResource(id = R.string.label_trigger).uppercase(),
                    text = trigger,
                    isLast = false
                )
                DominoTimelineStep(
                    title = stringResource(id = R.string.label_impact).uppercase(),
                    text = impact,
                    isLast = false
                )
                DominoTimelineStep(
                    title = stringResource(id = R.string.label_outlook).uppercase(),
                    text = outlook,
                    isLast = true
                )
            }
        }
    }
}

/**
 * Helper row for the DominoCard to align labels and values.
 */
@Composable
private fun DominoTimelineStep(title: String, text: String, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(dimensionResource(id = R.dimen.timeline_node_width))
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.timeline_dot_size))
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.padding_tiny))
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_standard)))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else dimensionResource(id = R.dimen.padding_xlarge))) {
            // 💡 colorScheme.primary, matching this card's own "DOMINO EFFECT" header above --
            // was onSurfaceMuted, which read as a disconnected label family from the header it
            // sits under (same fix applied to every other per-entry kicker of this shape).
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


/**
 * A row headline with the same trailing up/down arrow Market Sentiment uses, shown only when the
 * row actually has something to reveal ([expandable]).
 */
@Composable
private fun ExpandableRowHeader(
    expanded: Boolean,
    expandable: Boolean,
    headline: @Composable () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) { headline() }
        if (expandable) {
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            Icon(
                painter = painterResource(id = if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large))
            )
        }
    }
}

/**
 * The neutral tag/timeframe pill shared by [MacroMixSection] and [WatchSection] -- one component, not
 * two independently-styled ones, so a Macro Mix tag and a Watch timeframe read as the same visual
 * language rather than two different chips that happen to share a color.
 */
@Composable
private fun TagPill(text: String) {
    val pulseColors = LocalPulseColors.current
    Surface(
        color = pulseColors.accentPrimary,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = pulseColors.accentOn,
            modifier = Modifier.padding(
                horizontal = dimensionResource(id = R.dimen.padding_medium),
                vertical = dimensionResource(id = R.dimen.padding_tiny)
            )
        )
    }
}

/**
 * The small trailing chevron marking a [SignalPill] as a tap target that opens a glossary sheet
 * scoped to just that term -- regime, setup, and cycle-zone chips all use this. `contentDescription`
 * is null (decorative): the pill's own text is already what a screen reader announces for this tap
 * target, so a second "chevron forward" announcement on top of it would be redundant, unlike
 * [DriversSection]'s trailing navigate chevron, which isn't paired with its own adjacent label text.
 */
@Composable
private fun GlossaryChevron(tint: Color) {
    Icon(
        painter = painterResource(id = R.drawable.ic_chevron_forward),
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
    )
}

// ---------------------------------------------------------
// PREVIEWS
// ---------------------------------------------------------

private val previewVerdict = MarketVerdict(
    regime = MarketRegime.SIDEWAYS_RANGE,
    setup = TechnicalSetup.NEUTRAL_MEAN,
    signalLine = "Breaking Middle East maritime escalations and a dead US-Iran ceasefire are injecting stagflationary oil risks directly into a domestic consumer slowdown.",
    analysis = "The deterministic signal correctly identifies a completely neutral tape, requiring a continuation of the SIDEWAYS RANGE regime.",
    posture = "Capital is hiding in short-duration paper and defensive staples, refusing to commit to cyclical growth.",
    direction = SignalDirection.MIXED,
    conviction = Conviction.LOW,
    convictionReason = "Signals mixed -- no clear directional consensus."
)

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewSignalSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            SignalSection(
                verdict = previewVerdict,
                onRegimeClick = {},
                onDirectionClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewSignalSectionReadExpanded() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            SignalSection(
                verdict = previewVerdict,
                onRegimeClick = {},
                onDirectionClick = {},
                readExpanded = true
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewDriversSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            DriversSection(
                drivers = listOf(
                    MarketDriver(
                        label = "Retail Sales (MoM)",
                        direction = SignalColor.RED,
                        category = IndicatorCategory.MACRO_ECONOMY,
                        impact = DriverImpact.HIGH
                    ),
                    MarketDriver(
                        label = "Crude Oil",
                        direction = SignalColor.GREEN,
                        category = IndicatorCategory.SYSTEMIC_RISK,
                        impact = DriverImpact.HIGH
                    ),
                    MarketDriver(
                        label = "Credit Spreads (High Yield)",
                        direction = SignalColor.GREEN,
                        category = IndicatorCategory.SYSTEMIC_RISK,
                        impact = DriverImpact.MODERATE
                    ),
                    MarketDriver(
                        label = "Nonfarm Payrolls",
                        direction = SignalColor.RED,
                        category = IndicatorCategory.MACRO_ECONOMY,
                        impact = DriverImpact.MODERATE
                    )
                ),
                onClick = {},
                onInfoClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewMarketPositionSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            MarketPositionSection(
                position = MarketPosition(
                    positioning = Positioning(
                        pctFrom52wHigh = -1.28,
                        pctFrom52wLow = 21.51,
                        rangePosition = 93.17,
                        extensionPercentile1y = 96.4,
                        windowLabel = "251-day",
                        signalText = "Extended Near Highs",
                        signalColor = SignalColor.RED
                    ),
                    valuation = Valuation(pePercentile5y = 71.0),
                    cycleZone = "TRANSITION ZONE"
                ),
                setup = TechnicalSetup.OVERBOUGHT,
                whatChanged = "Retail sales missed, pulling forward Fed cut odds.",
                onSetupClick = {},
                onCycleZoneClick = {},
                onShowIndicatorsClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewWhatsNewSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            WhatsNewSection(
                listOf(
                    WhatsNewItem(
                        label = "Retail Sales (MoM)",
                        category = IndicatorCategory.MACRO_ECONOMY,
                        valueDisplay = "-0.3%",
                        changeDisplay = "-0.5%",
                        signalText = "Below Consensus",
                        signalColor = SignalColor.RED,
                        releaseDate = "2026-08-20"
                    ),
                    WhatsNewItem(
                        label = "Initial Jobless Claims",
                        category = IndicatorCategory.MACRO_ECONOMY,
                        valueDisplay = "235k",
                        changeDisplay = "-5k",
                        signalText = "In Line",
                        signalColor = SignalColor.YELLOW,
                        releaseDate = "2026-08-19"
                    ),
                    WhatsNewItem(
                        label = "Core PCE (MoM)",
                        category = IndicatorCategory.MACRO_ECONOMY,
                        valueDisplay = "0.1%",
                        changeDisplay = "-0.1%",
                        signalText = "Cooler Than Expected",
                        signalColor = SignalColor.GREEN,
                        releaseDate = "2026-08-15"
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewWatchSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            WatchSection(
                listOf(
                    WatchItem(
                        label = "CENTCOM Maritime Interdictions",
                        timeframe = "Over the next 48 hours",
                        why = "Any transition to kinetic engagement will spike war-risk insurance premiums."
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewRisksSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            RisksSection(
                listOf(
                    RiskItem(
                        risk = "Strait of Hormuz Blockade or Retaliation",
                        severity = RiskImpactLevel.HIGH,
                        note = "Aggressive US vessel boardings create a hair-trigger environment for a maritime clash."
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewDominoCard() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DominoCard(
                domino = DominoEffect(
                    trigger = "Expiration of the US-Iran memorandum of understanding and subsequent CENTCOM commercial vessel interdictions in the Gulf.",
                    impact = "Crude oil catches a sustained bid on supply chain fears, elevating breakeven inflation expectations.",
                    outlook = "This is a trend shift toward structurally higher geopolitical risk premiums."
                )
            )
        }
    }
}
