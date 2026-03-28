package com.marketlabs.pulse.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.screens.dashboard.views.DashboardRoute
import com.marketlabs.pulse.ui.components.PulseWebViewScreen
import com.marketlabs.pulse.ui.screens.indicators.views.IndicatorsRoute
import com.marketlabs.pulse.ui.screens.news.views.NewsRoute
import com.marketlabs.pulse.ui.screens.riskRadar.views.RiskRadarRoute
import com.marketlabs.pulse.ui.screens.summary.views.MarketSummaryRoute
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Centralize and define the navigation structure and logic for MarketLabs Pulse using Compose Navigation.
 * Action: Replace XML Fragments with Composables and implement a Scaffolding with Bottom Navigation.
 */

/** Store Navigation Route constants */
object PulseRoutes {
    const val MARKET_SUMMARY = "market_summary"
    const val MARKET_OVERVIEW = "market_overview"
    const val MARKET_INDICATORS = "market_indicators"
    const val MARKET_RISK = "market_risk"
    const val MARKET_NEWS = "market_news"
}

/** Define the data structure for Bottom Navigation items */
sealed class BottomNavItem(val route: String, val label: String, val iconRes: Int) {
    internal object Overview : BottomNavItem(PulseRoutes.MARKET_OVERVIEW, "Overview", R.drawable.ic_overview)
    internal object Indicators :
        BottomNavItem(PulseRoutes.MARKET_INDICATORS, "Indicators", R.drawable.ic_indicators)

    internal object Summary : BottomNavItem(PulseRoutes.MARKET_SUMMARY, "Summary", R.drawable.ic_summary)
    internal object RiskRadar : BottomNavItem(PulseRoutes.MARKET_RISK, "Risk", R.drawable.ic_risk)
    internal object News : BottomNavItem(PulseRoutes.MARKET_NEWS, "News", R.drawable.ic_news)

}

@Composable
fun PulseNavGraph() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Overview,
        BottomNavItem.Indicators,
        BottomNavItem.Summary,
        BottomNavItem.RiskRadar,
        BottomNavItem.News
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                // Clear default insets so we control the exact padding
                windowInsets = WindowInsets(15, 15, 15, 70)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    NavigationBarItem(
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.label
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PulseRoutes.MARKET_OVERVIEW,
            // Action: Remove padding here to allow the screen to expand using fillMaxSize
            modifier = Modifier.fillMaxSize()
        ) {
            /** Market Summary Route: Host the AI-generated reports */
            composable(PulseRoutes.MARKET_SUMMARY) {
                MarketSummaryRoute(scaffoldPadding = innerPadding)
            }
            composable(PulseRoutes.MARKET_OVERVIEW) {
                DashboardRoute(scaffoldPadding = innerPadding)
            }
            composable(PulseRoutes.MARKET_INDICATORS) {
                IndicatorsRoute(scaffoldPadding = innerPadding)
            }
            composable(PulseRoutes.MARKET_RISK) {
                RiskRadarRoute(scaffoldPadding = innerPadding)
            }
            composable(PulseRoutes.MARKET_NEWS) {
                NewsRoute(
                    scaffoldPadding = innerPadding,
                    // for the webviews
                    onNavigateToWebView = { url ->
                        val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                        navController.navigate("webview/$encodedUrl")
                    }
                )
            }
            // The WebView Destination
            composable("webview/{encodedUrl}") { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("encodedUrl") ?: ""
                val decodedUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())

                PulseWebViewScreen(
                    url = decodedUrl,
                    bottomNavPadding = innerPadding,
                    onNavigateUp = { navController.popBackStack() }
                )
            }
        }
    }
}