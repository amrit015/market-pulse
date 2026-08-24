package com.marketlabs.pulse.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseWebViewScreen
import com.marketlabs.pulse.ui.screens.dashboard.views.DashboardRoute
import com.marketlabs.pulse.ui.screens.indicators.views.IndicatorHorizonsRoute
import com.marketlabs.pulse.ui.screens.indicators.views.IndicatorsRoute
import com.marketlabs.pulse.ui.screens.insights.views.InsightsRoute
import com.marketlabs.pulse.ui.screens.news.views.NewsRoute
import com.marketlabs.pulse.ui.screens.stocks.detail.StockDetailRoute
import com.marketlabs.pulse.ui.screens.stocks.views.StockAnalysisRoute
import com.marketlabs.pulse.ui.screens.summary.views.MarketSummaryRoute
import com.marketlabs.pulse.ui.settings.SettingsRoute
import com.marketlabs.pulse.utils.enums.ReportType
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Store Navigation Route constants */
object PulseRoutes {
    const val MARKET_SUMMARY = "market_summary"
    const val MARKET_OVERVIEW = "market_overview"
    const val MARKET_INDICATORS = "market_indicators"
    const val MARKET_INSIGHTS = "market_insights"

    // Added with Claude Code assistance: no longer a bottom-nav tab, only reachable by pushing
    // from the Dashboard's news preview.
    const val MARKET_NEWS = "market_news"

    // Added with Claude Code assistance: replaces the News tab on the bottom bar.
    const val MARKET_ANALYSIS = "market_analysis"

    // Pushed from a StockPreviewCard tap on the Analysis tab. "symbol" is a required nav argument,
    // not a query param -- see StockDetailViewModel's SavedStateHandle read.
    const val STOCK_ANALYSIS_DETAIL = "stockAnalysis"

    // Reached from the gear icon on the global top bar.
    const val SETTINGS = "settings"

    // Pushed from the Indicators tab's "Horizons" entry card. Was local Compose state inside
    // IndicatorsScreen.kt; promoted to a real destination so it gets the same pushed-screen
    // treatment as Settings/News/Stock Detail -- its own header, no global top bar or floating
    // nav stacked underneath it.
    const val INDICATOR_HORIZONS = "indicator_horizons"
}

/** * 💡 UPDATED: Added a second icon resource for the 'selected' filled state
 * (You will need to ensure you have these filled versions in your res/drawable folder)
 */
sealed class BottomNavItem(val route: String, val label: String, val unselectedIconRes: Int, val selectedIconRes: Int) {
    internal object Overview :
        BottomNavItem(PulseRoutes.MARKET_OVERVIEW, "Overview", R.drawable.ic_dashboard_grid, R.drawable.ic_dashboard_grid_filled)

    internal object Indicators :
        BottomNavItem(PulseRoutes.MARKET_INDICATORS, "Indicators", R.drawable.ic_indicators, R.drawable.ic_indicators_filled)

    internal object Summary :
        BottomNavItem(PulseRoutes.MARKET_SUMMARY, "Summary", R.drawable.ic_ai_sparkle, R.drawable.ic_ai_sparkle_filled)

    internal object Insights :
        BottomNavItem(PulseRoutes.MARKET_INSIGHTS, "Insights", R.drawable.ic_insights, R.drawable.ic_insights_filled)

    // Added with Claude Code assistance: replaces News on the bottom bar. No filled variant
    // exists for this icon, so it's reused for both states — same as `Summary` above.
    internal object Analysis :
        BottomNavItem(PulseRoutes.MARKET_ANALYSIS, "Analysis", R.drawable.ic_analysis_trend, R.drawable.ic_analysis_trend_filled)
}

/**
 * Hoisted to a top-level `val` (was previously rebuilt on every recomposition as a local `val`
 * inside `PulseNavGraph()`) since `MainActivity` also needs this exact list to drive
 * `FloatingBottomNav`.
 */
val bottomNavItems = listOf(
    BottomNavItem.Overview,
    BottomNavItem.Indicators,
    BottomNavItem.Summary,
    BottomNavItem.Insights,
    BottomNavItem.Analysis
)

/**
 * NavHost-only. The `Scaffold`, the bottom `NavigationBar`, and `navController` creation all live
 * in `MainActivity` instead, since the global top bar and floating nav are app-wide chrome that
 * wraps this graph rather than something the graph owns itself — `MainActivity` needs
 * `navController` directly to drive `FloatingBottomNav`'s selected-tab state and `AppTopBar`'s
 * gear navigation.
 */
@Composable
fun PulseNavGraph(
    navController: NavHostController,
    scaffoldPadding: PaddingValues,
    // 💡 MainActivity owns the "did we arrive at Indicators via Drivers" flag -- this graph
    // reports the event up (onDriversNavigatedToIndicators) and reads the flag back down
    // (reachedIndicatorsFromDrivers) to place the BackHandler that consumes it, since that
    // handler has to live *inside* the Indicators destination's own content to take priority
    // over NavHost's own internal back handling (see the composable(MARKET_INDICATORS) block
    // below for why) -- it can't be composed up in MainActivity itself.
    onDriversNavigatedToIndicators: () -> Unit = {},
    reachedIndicatorsFromDrivers: Boolean = false,
    onIndicatorsBackHandled: () -> Unit = {},
    // 💡 Same up-reporting shape as onDriversNavigatedToIndicators above -- MainActivity's top bar
    // needs the loaded ReportType to show "Daily Update"/"Weekend Update" instead of a fixed
    // "Summary", but can't reach into MarketSummaryViewModel's state directly.
    onSummaryReportTypeLoaded: (ReportType?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Added with Claude Code assistance: one-shot signal set right before navigating to the News
    // tab from a Dashboard preview card, so NewsRoute knows which card to scroll to + highlight.
    // Hoisted here (not a nav argument) so the bottom-nav's plain "market_news" route pattern —
    // and its selected-tab matching in the bar above — stays untouched.
    var highlightedNewsArticleUrl by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = PulseRoutes.MARKET_OVERVIEW,
        modifier = modifier.fillMaxSize()
    ) {
        composable(PulseRoutes.MARKET_SUMMARY) {
            MarketSummaryRoute(
                scaffoldPadding = scaffoldPadding,
                onReportTypeLoaded = onSummaryReportTypeLoaded,
                // 💡 Back to the same tab-preserving popUpTo/saveState/restoreState dance
                // FloatingBottomNav's onItemClick uses -- a plain push here (tried first) reached
                // Indicators through a different code path than the bottom nav ever uses for that
                // same route, and mixing "plain push" and "restoreState tab switch" navigation to
                // one destination left Navigation-Compose's saved-state bookkeeping confused:
                // tapping the Summary tab afterward silently did nothing, staying on Indicators.
                // Going back to this identical mechanism makes the Drivers jump indistinguishable
                // from an ordinary tab switch as far as the nav library is concerned. "Back should
                // return to Summary, not Overview" is handled separately now, by the BackHandler
                // inside the MARKET_INDICATORS destination below, rather than by the navigation
                // *mechanism* itself.
                onNavigateToIndicators = {
                    onDriversNavigatedToIndicators()
                    navController.navigate(PulseRoutes.MARKET_INDICATORS) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(PulseRoutes.MARKET_OVERVIEW) {
            DashboardRoute(
                scaffoldPadding = scaffoldPadding,
                // Added with Claude Code assistance: News is a plain push destination now
                // (not a persisted bottom-nav tab), so this is a simple navigate() — no
                // popUpTo/saveState/restoreState tab-preserving dance needed.
                onNavigateToNews = {
                    navController.navigate(PulseRoutes.MARKET_NEWS)
                },
                // Added with Claude Code assistance: stash the target article, then push News.
                onNavigateToNewsArticle = { url ->
                    highlightedNewsArticleUrl = url
                    navController.navigate(PulseRoutes.MARKET_NEWS)
                }
            )
        }
        composable(PulseRoutes.MARKET_INDICATORS) {
            // 💡 Composed here, inside the destination's own content, not up in MainActivity --
            // NavHost registers its own internal back handling as part of composing itself, so a
            // BackHandler composed (and thus added to the back-press dispatcher) *before* NavHost
            // always loses to it; one added *after*, from inside the active destination's own
            // content, is what actually takes priority for that screen. Only enabled when Drivers
            // was the way here (see PulseNavGraph's onNavigateToIndicators above) -- an ordinary
            // tab-click arrival at Indicators keeps default back behavior (back to Overview).
            BackHandler(enabled = reachedIndicatorsFromDrivers) {
                onIndicatorsBackHandled()
                navController.navigate(PulseRoutes.MARKET_SUMMARY) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            IndicatorsRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateToHorizons = { navController.navigate(PulseRoutes.INDICATOR_HORIZONS) }
            )
        }
        // Pushed from the Indicators tab's "Horizons" entry card -- see PulseRoutes.INDICATOR_HORIZONS.
        composable(PulseRoutes.INDICATOR_HORIZONS) {
            IndicatorHorizonsRoute(onNavigateUp = { navController.popBackStack() })
        }
        composable(PulseRoutes.MARKET_INSIGHTS) {
            InsightsRoute(scaffoldPadding = scaffoldPadding)
        }
        // 💡 Updated with Claude Code assistance: the stocks domain layer was rebuilt against
        // the backend's new preview/detail split (see core/stocks, storage/model/stocks), and
        // this tab now renders the real preview list against it -- the temporary placeholder
        // that stood in for this tab is gone.
        composable(PulseRoutes.MARKET_ANALYSIS) {
            StockAnalysisRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateToDetail = { symbol ->
                    navController.navigate("${PulseRoutes.STOCK_ANALYSIS_DETAIL}/$symbol")
                }
            )
        }
        // Pushed from a StockPreviewCard tap. "symbol" is read out of SavedStateHandle by
        // StockDetailViewModel itself (see StockDetailViewModel.ARG_SYMBOL), not passed as a
        // composable parameter here -- hiltViewModel() auto-populates it from this route.
        composable("${PulseRoutes.STOCK_ANALYSIS_DETAIL}/{symbol}") {
            StockDetailRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateUp = { navController.popBackStack() },
                // 💡 Same in-app webview route News' article cards push to -- Direct News' cards
                // open the same way rather than each screen owning its own way of leaving the app.
                onNavigateToWebView = { url ->
                    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    navController.navigate("webview/$encodedUrl")
                }
            )
        }
        // Added with Claude Code assistance: pushed only from the Dashboard's "Latest News"
        // chevron or a specific preview card — no longer part of the bottom bar.
        composable(PulseRoutes.MARKET_NEWS) {
            NewsRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateUp = { navController.popBackStack() },
                onNavigateToWebView = { url ->
                    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    navController.navigate("webview/$encodedUrl")
                },
                highlightedArticleUrl = highlightedNewsArticleUrl,
                onHighlightConsumed = { highlightedNewsArticleUrl = null }
            )
        }

        composable("webview/{encodedUrl}") { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("encodedUrl") ?: ""
            val decodedUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())

            PulseWebViewScreen(
                url = decodedUrl,
                bottomNavPadding = scaffoldPadding,
                onNavigateUp = { navController.popBackStack() }
            )
        }

        // Reached from the gear icon on the global top bar.
        composable(PulseRoutes.SETTINGS) {
            SettingsRoute(onNavigateUp = { navController.popBackStack() })
        }
    }
}
