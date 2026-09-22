package com.marketlabs.pulse.ui.screens.insights.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.DisclaimerFooter
import com.marketlabs.pulse.ui.screens.insights.InsightsUiState
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * The 4 sections -- Playbook, Risks, Posture, Positioning, in that order -- are one `PulseTabRow`
 * tab apiece. Mirrors `StockDetailScreen`'s `DetailTab` pattern exactly: an enum with a `labelRes`,
 * a `selectedTabIndex` driven by the ViewModel, and each tab as its own `LazyColumn` with its own
 * `LazyListState` so scroll position is preserved per tab when switching back and forth.
 *
 * 💡 The tab content is a `HorizontalPager`, so a reader can swipe left/right between sections in
 * addition to tapping `PulseTabRow`. `pagerState` is created and kept in sync with the ViewModel's
 * `selectedTabIndex` up in `InsightsRoute` (tap -> animateScrollToPage; swipe -> onTabSelected),
 * since that's the one place both `PulseTabRow` and this screen are composed together. Deliberately
 * NOT pushed into `PulseTabRow` itself, which stays a plain tab bar.
 */
enum class InsightsTab(val labelRes: Int) {
    PLAYBOOK(R.string.insights_tab_playbook),
    RISKS(R.string.insights_tab_risks),
    POSTURE(R.string.insights_tab_posture),
    POSITIONING(R.string.insights_tab_positioning)
}

@Composable
fun InsightsScreen(
    uiState: InsightsUiState,
    pagerState: PagerState,
    scaffoldPadding: PaddingValues,
    onNavigateToGlossaryDetail: (metricIds: List<String>, chartMetricId: String, title: String, description: String?, status: String?) -> Unit,
    onDismissPositioningIntro: () -> Unit,
    onDismissPostureIntro: () -> Unit
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    // 💡 `top` is just breathing room under the pinned PulseTabRow now, not `scaffoldPadding`'s top
    // component -- the global top bar's own inset is already consumed once by that pinned tab row
    // (rendered above this screen in `InsightsRoute`), so adding it again here would double the gap
    // between the top bar and the tab row's own content.
    val contentPadding = PaddingValues(
        top = paddingLarge,
        bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge,
        start = paddingLarge,
        end = paddingLarge
    )
    val lazyListStates = remember { List(InsightsTab.entries.size) { LazyListState() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (InsightsTab.entries[page]) {
                // 💡 padding_extra_large gap before the footer on all four tabs below -- same
                // value every other screen's DisclaimerFooter sits below; each of these
                // LazyColumns only ever has the one content item plus the footer, so spacedBy here
                // affects just that one gap.
                InsightsTab.PLAYBOOK -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListStates[InsightsTab.PLAYBOOK.ordinal],
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_extra_large))
                ) {
                    item {
                        val playbook = uiState.weeklyPlaybook
                        if (playbook != null && !playbook.events.isNullOrEmpty()) {
                            WeeklyPlaybookSection(playbook = playbook)
                        } else {
                            InsightsTabEmptyState()
                        }
                    }
                    item { DisclaimerFooter(showAiDisclosure = true) }
                }

                InsightsTab.RISKS -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListStates[InsightsTab.RISKS.ordinal],
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_extra_large))
                ) {
                    item {
                        val risksData = uiState.tailRisks
                        if (risksData != null) {
                            TailRisksSection(risksData = risksData)
                        } else {
                            InsightsTabEmptyState()
                        }
                    }
                    item { DisclaimerFooter(showAiDisclosure = true) }
                }

                InsightsTab.POSTURE -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListStates[InsightsTab.POSTURE.ordinal],
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_extra_large))
                ) {
                    item {
                        val postureData = uiState.marketPosture
                        if (postureData != null) {
                            InstitutionalPostureSection(
                                postureData = postureData,
                                onNavigateToGlossaryDetail = onNavigateToGlossaryDetail,
                                isIntroDismissed = uiState.isPostureIntroDismissed,
                                onDismissIntro = onDismissPostureIntro
                            )
                        } else {
                            InsightsTabEmptyState()
                        }
                    }
                    item { DisclaimerFooter() }
                }

                InsightsTab.POSITIONING -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListStates[InsightsTab.POSITIONING.ordinal],
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_extra_large))
                ) {
                    item {
                        val positioningData = uiState.marketPositioning
                        if (positioningData != null) {
                            MarketPositioningSection(
                                positioningData = positioningData,
                                onNavigateToGlossaryDetail = onNavigateToGlossaryDetail,
                                isIntroDismissed = uiState.isPositioningIntroDismissed,
                                onDismissIntro = onDismissPositioningIntro
                            )
                        } else {
                            InsightsTabEmptyState()
                        }
                    }
                    item { DisclaimerFooter() }
                }
            }
        }
    }
}

/**
 * Same "nothing loaded yet" treatment `NewsScreen` uses for its own empty state -- centered, muted,
 * pull-to-refresh points at the fix. `LazyItemScope` receiver (not a plain `@Composable`) since
 * `fillParentMaxSize()` -- sizing to the LazyColumn's own viewport rather than shrink-wrapping to
 * this one empty-state item -- is only available inside a `LazyListScope.item { }` block.
 */
@Composable
private fun LazyItemScope.InsightsTabEmptyState() {
    Box(
        modifier = Modifier.fillParentMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.insights_tab_empty_state),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Empty state", showBackground = true)
@Composable
private fun PreviewInsightsScreenEmpty() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        InsightsScreen(
            uiState = InsightsUiState(),
            pagerState = rememberPagerState { InsightsTab.entries.size },
            scaffoldPadding = PaddingValues(),
            onNavigateToGlossaryDetail = { _, _, _, _, _ -> },
            onDismissPositioningIntro = {},
            onDismissPostureIntro = {}
        )
    }
}
