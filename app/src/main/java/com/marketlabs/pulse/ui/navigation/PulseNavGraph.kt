package com.marketlabs.pulse.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseWebViewScreen
import com.marketlabs.pulse.ui.screens.dashboard.views.DashboardRoute
import com.marketlabs.pulse.ui.screens.indicators.views.IndicatorsRoute
import com.marketlabs.pulse.ui.screens.insights.views.InsightsRoute
import com.marketlabs.pulse.ui.screens.news.views.NewsRoute
import com.marketlabs.pulse.ui.screens.summary.views.MarketSummaryRoute
import com.marketlabs.pulse.ui.settings.SettingsRoute
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

    // 💡 Added for spec-20260809-theme-migration: reached from the global top bar gear.
    const val SETTINGS = "settings"
}

/** * 💡 UPDATED: Added a second icon resource for the 'selected' filled state
 * (You will need to ensure you have these filled versions in your res/drawable folder)
 */
sealed class BottomNavItem(val route: String, val label: String, val unselectedIconRes: Int, val selectedIconRes: Int) {
    internal object Overview :
        BottomNavItem(PulseRoutes.MARKET_OVERVIEW, "Overview", R.drawable.ic_overview, R.drawable.ic_overview_filled)

    internal object Indicators :
        BottomNavItem(PulseRoutes.MARKET_INDICATORS, "Indicators", R.drawable.ic_indicators, R.drawable.ic_indicators_filled)

    internal object Summary :
        BottomNavItem(PulseRoutes.MARKET_SUMMARY, "Summary", R.drawable.ic_engine_ai_sparkles, R.drawable.ic_engine_ai_sparkles)

    internal object MarketRisk :
        BottomNavItem(PulseRoutes.MARKET_INSIGHTS, "Insights", R.drawable.ic_risk, R.drawable.ic_risk_filled)

    // Added with Claude Code assistance: replaces News on the bottom bar. No filled variant
    // exists for this icon, so it's reused for both states — same as `Summary` above.
    internal object Analysis :
        BottomNavItem(PulseRoutes.MARKET_ANALYSIS, "Analysis", R.drawable.ic_engine_quant, R.drawable.ic_engine_quant)
}

/**
 * 💡 Added for spec-20260809-theme-migration: hoisted out of `PulseNavGraph()` (was previously
 * rebuilt on every recomposition as a local `val`) since `MainActivity` now also needs this exact
 * list to drive `FloatingBottomNav`.
 */
val bottomNavItems = listOf(
    BottomNavItem.Overview,
    BottomNavItem.Indicators,
    BottomNavItem.Summary,
    BottomNavItem.MarketRisk,
    BottomNavItem.Analysis
)

/**
 * 💡 Added with Claude Code assistance: temporary stand-in for the Analysis tab while its views
 * are rebuilt against the revamped stocks domain layer (core/stocks, storage/model/stocks) —
 * the old StockAnalysisRoute/Screen/ViewModel were deleted, not patched, since they were written
 * against a network shape the backend no longer serves.
 */
@Composable
private fun StockAnalysisPlaceholder(scaffoldPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.market_analysis_placeholder_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 💡 Updated for spec-20260809-theme-migration: this is now NavHost-only. The `Scaffold`, the
 * bottom `NavigationBar`, and `navController` creation all moved up to `MainActivity`, since the
 * new global top bar and floating nav are app-wide chrome that wraps this graph rather than
 * something the graph owns itself — `MainActivity` needs `navController` directly to drive
 * `FloatingBottomNav`'s selected-tab state and `AppTopBar`'s gear navigation.
 */
@Composable
fun PulseNavGraph(
    navController: NavHostController,
    scaffoldPadding: PaddingValues,
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
            MarketSummaryRoute(scaffoldPadding = scaffoldPadding)
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
            IndicatorsRoute(scaffoldPadding = scaffoldPadding)
        }
        composable(PulseRoutes.MARKET_INSIGHTS) {
            InsightsRoute(scaffoldPadding = scaffoldPadding)
        }
        // 💡 Updated with Claude Code assistance: the stocks domain layer was rebuilt against
        // the backend's new preview/detail split (see core/stocks, storage/model/stocks) and
        // its old views were deleted wholesale rather than patched — StockAnalysisRoute and
        // friends no longer exist. This placeholder keeps the Analysis tab compiling and
        // navigable until the screen is rebuilt against the new domain layer.
        composable(PulseRoutes.MARKET_ANALYSIS) {
            StockAnalysisPlaceholder(scaffoldPadding = scaffoldPadding)
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

        // 💡 Added for spec-20260809-theme-migration: reached from the global top bar gear.
        composable(PulseRoutes.SETTINGS) {
            SettingsRoute(onNavigateUp = { navController.popBackStack() })
        }
    }
}
