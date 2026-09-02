package com.marketlabs.pulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marketlabs.pulse.data.theme.ThemeRepository
import com.marketlabs.pulse.ui.components.AppTopBar
import com.marketlabs.pulse.ui.components.FloatingBottomNav
import com.marketlabs.pulse.ui.navigation.PulseNavGraph
import com.marketlabs.pulse.ui.navigation.PulseRoutes
import com.marketlabs.pulse.ui.navigation.bottomNavItems
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.utils.enums.ReportType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * 💡 THOUGHT PROCESS:
 * Owns the app-wide chrome: the `Scaffold`, the global collapsing `AppTopBar`, and the floating
 * `FloatingBottomNav` all live here rather than inside `PulseNavGraph`, since they wrap every
 * screen rather than belonging to any one of them. `navController` is created here (not inside the
 * graph) so it can drive both the top bar's gear navigation and the floating nav's selected-tab
 * highlighting.
 *
 * `ThemeRepository` is field-injected directly into the Activity (standard Hilt pattern for
 * `@AndroidEntryPoint`) rather than through a ViewModel — this is app-wide, cross-screen state read
 * exactly once at the composition root, not a single screen's concern, so a dedicated ViewModel
 * would only add ceremony. `MarketPulseTheme` (the composable) stays Hilt-agnostic by design; this
 * is the one place that bridges the persisted preset into it.
 *
 * `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)` is attached once, on the
 * `Scaffold`'s own modifier — the standard Compose Material 3 pattern for `enterAlwaysScrollBehavior`.
 * Nested-scroll dispatch bubbles up through the composition tree from whichever tab's `LazyColumn`/
 * `verticalScroll` is actually producing scroll deltas, so this single attachment point is enough;
 * no individual screen file needed touching for the collapsing behavior itself.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeRepository: ThemeRepository

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 💡 `collectAsStateWithLifecycle`'s `initialValue` is what the very first Compose frame
        // renders with, before `themeRepository.selectedTheme` (backed by DataStore, an async disk
        // read on a cold process start) has emitted anything at all. That used to be hardcoded to
        // `MarketPulseTheme.LILAC` -- correct for a brand-new user with no persisted preference
        // (LILAC really is the first thing the repository itself emits for them, see
        // `ThemeRepositoryImpl.DEFAULT_THEME`), but wrong for anyone who already picked a different
        // theme: their real preference hadn't loaded from disk yet, so the app briefly painted
        // LILAC's dark status bar/chrome, then snapped to their actual (often light) theme once the
        // DataStore read finished a frame or two later -- the flash. Reading the real value
        // synchronously here instead, before `setContent`, means the first frame is already correct
        // and there's nothing to snap away from. `runBlocking` is safe for this specific case: it's
        // a single small Preferences value already backed by DataStore's in-process cache after the
        // first read this process makes, not an unbounded or network-backed read, and `onCreate` is
        // already blocking the main thread on layout inflation at this point regardless.
        val initialTheme = runBlocking { themeRepository.selectedTheme.first() }

        setContent {
            val selectedTheme by themeRepository.selectedTheme.collectAsStateWithLifecycle(
                initialValue = initialTheme
            )

            MarketPulseTheme(theme = selectedTheme) {
                val navController = rememberNavController()
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 💡 News, Settings, Indicator Horizons, the in-app web view, and the stock detail
                // screen are pushed, full-screen destinations reached by tapping something on a
                // bottom-nav tab, not a tab themselves -- they already own their own
                // top-of-screen chrome (see NewsRoute.kt, SettingsScreen.kt,
                // IndicatorHorizonsRoute.kt, WebViewScreen.kt, StockDetailRoute.kt's pinned
                // DetailHeader) and have no "current tab" for the floating nav to highlight.
                // Rendering the global collapsing bar on top of those was showing two bars stacked
                // at once and fighting the local one's back affordance, and the floating nav below
                // them was just extra chrome over a screen that already has its own way back -- so
                // both are left out of the Scaffold entirely for these routes, not just visually
                // collapsed. Stock detail's own header isn't a Material TopAppBar (too rich a
                // layout for that slot -- symbol/price/badges all in one pinned block), but the
                // same exclusion applies per the stock-analysis-ui spec's explicit "suppress the
                // collapsing top bar for this route" instruction.
                val isPushedDestination = currentRoute == PulseRoutes.MARKET_NEWS ||
                    currentRoute == PulseRoutes.SETTINGS ||
                    currentRoute == PulseRoutes.INDICATOR_HORIZONS ||
                    currentRoute?.startsWith("webview/") == true ||
                    currentRoute?.startsWith("${PulseRoutes.STOCK_ANALYSIS_DETAIL}/") == true ||
                    currentRoute?.startsWith("${PulseRoutes.ASSET_DETAIL}/") == true ||
                    currentRoute?.startsWith("${PulseRoutes.METRIC_DETAIL}/") == true ||
                    currentRoute?.startsWith("${PulseRoutes.GLOSSARY_DETAIL}/") == true

                // 💡 scrollBehavior.state.heightOffset is one shared value driving the top bar's
                // collapse amount across every tab (see this file's own header comment on why
                // there's only one nestedScroll attachment point). Each tab's own LazyColumn
                // scroll position is already correctly saved/restored per-route by Compose
                // Navigation's `restoreState`/`saveState` -- but this offset isn't tied to that,
                // so switching tabs used to carry the previous tab's collapsed-bar amount over
                // onto whichever tab you landed on, even a tab visited for the first time this
                // session (which should start fully expanded). Tracked here per-route instead:
                // save the outgoing route's offset before it's overwritten, restore the incoming
                // route's own last offset (or 0f/fully expanded, for a route with no history yet).
                val routeHeightOffsets = remember { mutableMapOf<String, Float>() }
                var lastRoute by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(currentRoute) {
                    lastRoute?.let { routeHeightOffsets[it] = scrollBehavior.state.heightOffset }
                    scrollBehavior.state.heightOffset = routeHeightOffsets[currentRoute] ?: 0f
                    lastRoute = currentRoute
                }

                // 💡 Summary's Drivers section jumps straight to Indicators via the same tab-
                // switch mechanism the bottom nav itself uses (see PulseNavGraph.kt's
                // onNavigateToIndicators) -- so, as far as the back stack is concerned, it's
                // indistinguishable from the user having tapped the Indicators tab directly, and
                // default back behavior would return to Overview (the start destination), not
                // Summary (where the user actually came from). This flag is the one place that
                // distinction is tracked: cleared the moment the route becomes anything other
                // than Indicators, so it never lingers into an unrelated, later visit to that tab
                // reached some other way. The BackHandler that reads it lives inside
                // PulseNavGraph's Indicators destination itself, not here -- NavHost registers
                // its own internal back handling as part of composing itself, so a BackHandler
                // declared up here (composed, and so added to the dispatcher, *before* NavHost)
                // always lost to it; one composed *inside* the destination's own content is added
                // after NavHost's, and only then actually takes priority for that screen.
                var reachedIndicatorsFromDrivers by remember { mutableStateOf(false) }
                LaunchedEffect(currentRoute) {
                    if (currentRoute != PulseRoutes.MARKET_INDICATORS) {
                        reachedIndicatorsFromDrivers = false
                    }
                }

                // spec-20260902-market-sentiment-android.md: same "arrived via a same-tab-switch
                // jump, not a real tab click" tracking as reachedIndicatorsFromDrivers above, for
                // the Market Sentiment card's jump to Insights (Posture) -- default back behavior
                // would otherwise return to Overview instead of Summary.
                var reachedInsightsFromMarketSentiment by remember { mutableStateOf(false) }
                LaunchedEffect(currentRoute) {
                    if (currentRoute != PulseRoutes.MARKET_INSIGHTS) {
                        reachedInsightsFromMarketSentiment = false
                    }
                }

                // 💡 Summary's own in-content header is gone (the report-type label used to live
                // there) -- the global top bar shows it instead now, reported up via
                // PulseNavGraph's onSummaryReportTypeLoaded (see MarketSummaryRoute.kt). Null
                // until that screen's data has loaded at least once this session, in which case
                // topBarTitle falls back to the fixed "Summary" string below.
                var summaryReportType by remember { mutableStateOf<ReportType?>(null) }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        if (!isPushedDestination) {
                            AppTopBar(
                                title = topBarTitle(currentRoute, summaryReportType),
                                scrollBehavior = scrollBehavior,
                                onSettingsClick = { navController.navigate(PulseRoutes.SETTINGS) }
                            )
                        }
                    },
                    bottomBar = {
                        // 💡 Reuses `isPushedDestination` rather than its own route list -- see the
                        // comment above it. One shared boolean means these two chrome decisions
                        // can't drift apart as new pushed routes are added later.
                        if (!isPushedDestination) {
                            FloatingBottomNav(
                                items = bottomNavItems,
                                currentRoute = currentRoute,
                                onItemClick = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    // 💡 `innerPadding` alone stays fixed at the top bar's fully-expanded height
                    // the whole time -- `enterAlwaysScrollBehavior` hides the bar by translating it
                    // off-screen at render time, which is not a layout change, so Scaffold's own
                    // padding measurement never shrinks. Using it as-is would leave a dead,
                    // background-colored gap where the bar used to be once it slides away, instead
                    // of letting content actually scroll up into that space so the (edge-to-edge,
                    // transparent) status bar shows real content behind it rather than empty space.
                    //
                    // Adding `scrollBehavior.state.heightOffset` in fixes this: it is 0 when the
                    // bar is fully expanded (so this equals `innerPadding` exactly, unchanged from
                    // before) and grows negative as the bar collapses, down to a limit Material 3
                    // computes net of the bar's own status-bar inset -- so this bottoms out exactly
                    // at the status bar's height once the bar is fully hidden, never less, without
                    // this needing to know that height itself.
                    val layoutDirection = LocalLayoutDirection.current
                    val density = LocalDensity.current
                    val dynamicTopPadding = with(density) {
                        (innerPadding.calculateTopPadding() + scrollBehavior.state.heightOffset.toDp())
                            .coerceAtLeast(0.dp)
                    }
                    val dynamicScaffoldPadding = PaddingValues(
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        top = dynamicTopPadding,
                        end = innerPadding.calculateEndPadding(layoutDirection),
                        bottom = innerPadding.calculateBottomPadding()
                    )

                    PulseNavGraph(
                        navController = navController,
                        scaffoldPadding = dynamicScaffoldPadding,
                        onDriversNavigatedToIndicators = { reachedIndicatorsFromDrivers = true },
                        reachedIndicatorsFromDrivers = reachedIndicatorsFromDrivers,
                        onIndicatorsBackHandled = { reachedIndicatorsFromDrivers = false },
                        onMarketSentimentNavigatedToInsights = { reachedInsightsFromMarketSentiment = true },
                        reachedInsightsFromMarketSentiment = reachedInsightsFromMarketSentiment,
                        onInsightsBackHandled = { reachedInsightsFromMarketSentiment = false },
                        onSummaryReportTypeLoaded = { summaryReportType = it }
                    )
                }
            }
        }
    }
}

/**
 * Resolves the global top bar's title from the current back-stack route. Mostly a static per-route
 * mapping -- `MainActivity` owns app-wide chrome and stays intentionally dumb about what any one
 * screen is doing internally -- with one exception: Market Summary's report type is only known
 * once that screen's data loads, so it's reported up via PulseNavGraph's
 * onSummaryReportTypeLoaded (see MarketSummaryRoute.kt) instead of a second static string here.
 * Falls back to the fixed "Summary" string until that first load completes.
 *
 * News and Settings have no branch here -- `isPushedDestination` above means this bar is never
 * composed for those routes at all, so a title for them would never be read.
 */
@Composable
private fun topBarTitle(route: String?, summaryReportType: ReportType?): String = when (route) {
    PulseRoutes.MARKET_INDICATORS -> stringResource(id = R.string.indicators_screen_title)
    PulseRoutes.MARKET_INSIGHTS -> stringResource(id = R.string.insights_screen_title)
    PulseRoutes.MARKET_SUMMARY -> summaryReportType?.label ?: stringResource(id = R.string.summary_screen_title)
    PulseRoutes.MARKET_ANALYSIS -> stringResource(id = R.string.market_analysis_screen_title)
    else -> stringResource(id = R.string.app_name)
}
