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
import androidx.compose.runtime.getValue
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
import dagger.hilt.android.AndroidEntryPoint
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

        setContent {
            val selectedTheme by themeRepository.selectedTheme.collectAsStateWithLifecycle(
                initialValue = MarketPulseTheme.LILAC
            )

            MarketPulseTheme(theme = selectedTheme) {
                val navController = rememberNavController()
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 💡 News, Settings, the in-app web view, and the stock detail screen are pushed,
                // full-screen destinations reached by tapping something on a bottom-nav tab, not a
                // tab themselves -- they already own their own top-of-screen chrome (see
                // NewsRoute.kt, SettingsScreen.kt, WebViewScreen.kt, StockDetailRoute.kt's pinned
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
                    currentRoute?.startsWith("webview/") == true ||
                    currentRoute?.startsWith("${PulseRoutes.STOCK_ANALYSIS_DETAIL}/") == true

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        if (!isPushedDestination) {
                            AppTopBar(
                                title = topBarTitle(currentRoute),
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
                        scaffoldPadding = dynamicScaffoldPadding
                    )
                }
            }
        }
    }
}

/**
 * Resolves the global top bar's title from the current back-stack route. A static per-route
 * mapping, not read from any screen's own ViewModel state -- `MainActivity` owns app-wide chrome
 * and stays intentionally dumb about what any one screen is doing internally. Market Summary's own
 * in-content header shows a data-dependent report-type label (only known once that screen's data
 * loads) that this can't replicate without reaching into its state, so it gets a fixed "Summary"
 * here instead; that in-content label stays exactly as it is, unlike Indicators/Insights' redundant
 * page-title rows, which are gone now that this bar carries the title.
 *
 * News and Settings have no branch here -- `isPushedDestination` above means this bar is never
 * composed for those routes at all, so a title for them would never be read.
 */
@Composable
private fun topBarTitle(route: String?): String = when (route) {
    PulseRoutes.MARKET_INDICATORS -> stringResource(id = R.string.indicators_screen_title)
    PulseRoutes.MARKET_INSIGHTS -> stringResource(id = R.string.insights_screen_title)
    PulseRoutes.MARKET_SUMMARY -> stringResource(id = R.string.summary_screen_title)
    PulseRoutes.MARKET_ANALYSIS -> stringResource(id = R.string.market_analysis_screen_title)
    else -> stringResource(id = R.string.app_name)
}
