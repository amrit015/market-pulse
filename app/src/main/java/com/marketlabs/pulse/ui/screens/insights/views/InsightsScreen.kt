package com.marketlabs.pulse.ui.screens.insights.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.screens.insights.InsightsUiState

/**
 * The 4 sections this screen used to stack in one long scroll (separated by dividers) are now one
 * `PulseTabRow` tab apiece -- Playbook, Risks, Posture, Positioning, in that order. Mirrors
 * `StockDetailScreen`'s `DetailTab` pattern exactly: an enum with a `labelRes`, a `selectedTabIndex`
 * driven by the ViewModel, and each tab as its own `LazyColumn` with its own `LazyListState` so
 * scroll position is preserved per tab when switching back and forth.
 *
 * 💡 Insights-only swipe (2026-08-29): the tab content is now a `HorizontalPager` rather than a
 * plain `when` switch on `selectedTabIndex`, so a reader can swipe left/right between sections in
 * addition to tapping `PulseTabRow`. `pagerState` is created and kept in sync with the ViewModel's
 * `selectedTabIndex` up in `InsightsRoute` (tap -> animateScrollToPage; swipe -> onTabSelected),
 * since that's the one place both `PulseTabRow` and this screen are composed together. Deliberately
 * NOT pushed into `PulseTabRow` itself -- that component is shared with Stock Analysis detail,
 * which doesn't ask for swipe, so this stays local to Insights for now.
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
    onNavigateToGlossaryDetail: (metricIds: List<String>, title: String, description: String?, status: String?) -> Unit,
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
                InsightsTab.PLAYBOOK -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListStates[InsightsTab.PLAYBOOK.ordinal],
                    contentPadding = contentPadding
                ) {
                    item {
                        val playbook = uiState.weeklyPlaybook
                        if (playbook != null && !playbook.events.isNullOrEmpty()) {
                            WeeklyPlaybookSection(playbook = playbook)
                        } else {
                            InsightsTabEmptyState()
                        }
                    }
                }

                InsightsTab.RISKS -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListStates[InsightsTab.RISKS.ordinal],
                    contentPadding = contentPadding
                ) {
                    item {
                        val risksData = uiState.tailRisks
                        if (risksData != null) {
                            TailRisksSection(risksData = risksData)
                        } else {
                            InsightsTabEmptyState()
                        }
                    }
                }

                InsightsTab.POSTURE -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListStates[InsightsTab.POSTURE.ordinal],
                    contentPadding = contentPadding
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
                }

                InsightsTab.POSITIONING -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListStates[InsightsTab.POSITIONING.ordinal],
                    contentPadding = contentPadding
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
