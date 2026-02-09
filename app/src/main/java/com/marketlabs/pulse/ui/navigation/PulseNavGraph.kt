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
import com.marketlabs.pulse.ui.compose.overview.MarketOverviewRoute
import com.marketlabs.pulse.ui.compose.summary.MarketSummaryRoute

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
    object Overview : BottomNavItem(PulseRoutes.MARKET_OVERVIEW, "Overview", R.drawable.ic_overview)
    object Indicators :
        BottomNavItem(PulseRoutes.MARKET_INDICATORS, "Indicators", R.drawable.ic_indicators)

    object Summary : BottomNavItem(PulseRoutes.MARKET_SUMMARY, "Summary", R.drawable.ic_summary)
    object RiskRadar : BottomNavItem(PulseRoutes.MARKET_RISK, "Risk", R.drawable.ic_risk)
    object News : BottomNavItem(PulseRoutes.MARKET_NEWS, "News", R.drawable.ic_news)

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
                // 2. Clear default insets so we control the exact padding
                windowInsets = WindowInsets(15, 15, 15, 60)
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
            startDestination = PulseRoutes.MARKET_SUMMARY,
            // Action: Remove padding here to allow the screen to expand using fillMaxSize
            modifier = Modifier.fillMaxSize()
        ) {
            /** Market Summary Route: Host the AI-generated reports */
            composable(PulseRoutes.MARKET_SUMMARY) {
                MarketSummaryRoute(scaffoldPadding = innerPadding)
            }

            /** Placeholder routes for other bottom nav destinations */
            composable(PulseRoutes.MARKET_OVERVIEW) {
                MarketOverviewRoute(scaffoldPadding = innerPadding)
            }
            composable(PulseRoutes.MARKET_INDICATORS) {
                // ScreenC()
            }
            composable(PulseRoutes.MARKET_RISK) {
                // ScreenD()
            }
            composable(PulseRoutes.MARKET_NEWS) {
                // ScreenE()
            }
        }
    }
}