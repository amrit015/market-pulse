package com.marketlabs.pulse.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseWebViewScreen
import com.marketlabs.pulse.ui.screens.dashboard.views.DashboardRoute
import com.marketlabs.pulse.ui.screens.indicators.views.IndicatorsRoute
import com.marketlabs.pulse.ui.screens.insights.views.InsightsRoute
import com.marketlabs.pulse.ui.screens.news.views.NewsRoute
import com.marketlabs.pulse.ui.screens.summary.views.MarketSummaryRoute
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Store Navigation Route constants */
object PulseRoutes {
    const val MARKET_SUMMARY = "market_summary"
    const val MARKET_OVERVIEW = "market_overview"
    const val MARKET_INDICATORS = "market_indicators"
    const val MARKET_INSIGHTS = "market_insights"
    const val MARKET_NEWS = "market_news"
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

    internal object News :
        BottomNavItem(PulseRoutes.MARKET_NEWS, "News", R.drawable.ic_news, R.drawable.ic_news_filled)
}

@Composable
fun PulseNavGraph() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Overview,
        BottomNavItem.Indicators,
        BottomNavItem.Summary,
        BottomNavItem.MarketRisk,
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
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                    // 💡 NEW: Smoothly animate the icon size from 24dp to 28dp when selected
                    val iconSize by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 24.dp,
                        label = "BottomNavIconSize"
                    )

                    NavigationBarItem(
                        label = { Text(item.label) },
                        selected = isSelected,
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
                            // 💡 NEW: Swap between the filled and outlined icons based on state
                            Icon(
                                painter = painterResource(id = if (isSelected) item.selectedIconRes else item.unselectedIconRes),
                                contentDescription = item.label,
                                modifier = Modifier.size(iconSize) // Apply the animated size
                            )
                        },
                        // 💡 NEW: Kill the default gray highlight pill
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PulseRoutes.MARKET_OVERVIEW,
            modifier = Modifier.fillMaxSize()
        ) {
            // ... (Keep all your existing composable routes exactly the same) ...
            composable(PulseRoutes.MARKET_SUMMARY) {
                MarketSummaryRoute(scaffoldPadding = innerPadding)
            }
            composable(PulseRoutes.MARKET_OVERVIEW) {
                DashboardRoute(scaffoldPadding = innerPadding)
            }
            composable(PulseRoutes.MARKET_INDICATORS) {
                IndicatorsRoute(scaffoldPadding = innerPadding)
            }
            composable(PulseRoutes.MARKET_INSIGHTS) {
                InsightsRoute(scaffoldPadding = innerPadding)
            }
            composable(PulseRoutes.MARKET_NEWS) {
                NewsRoute(
                    scaffoldPadding = innerPadding,
                    onNavigateToWebView = { url ->
                        val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                        navController.navigate("webview/$encodedUrl")
                    }
                )
            }
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