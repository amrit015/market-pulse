package com.marketlabs.pulse.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marketlabs.pulse.ui.compose.MarketSummaryRoute

/**
 * Centralize and define the navigation structure and logic for MarketLabs Pulse using Compose Navigation.
 * Action: Replace XML Fragments with Composables and implement a Scaffolding with Bottom Navigation.
 */

/** Store Navigation Route constants */
object PulseRoutes {
    const val MARKET_SUMMARY = "market_summary"
    const val FRAGMENT_B = "route_b"
    const val FRAGMENT_C = "route_c"
    const val FRAGMENT_D = "route_d"
    const val FRAGMENT_E = "route_e"
}

/** Define the data structure for Bottom Navigation items */
sealed class BottomNavItem(val route: String, val label: String) {
    object Summary : BottomNavItem(PulseRoutes.MARKET_SUMMARY, "Summary")
    object B : BottomNavItem(PulseRoutes.FRAGMENT_B, "Dashboard")
    // ... Define C, D, E similarly
}

@Composable
fun PulseNavGraph() {
    val navController = rememberNavController()
    val items = listOf(BottomNavItem.Summary, BottomNavItem.B /* add others */)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                // 2. Clear default insets so we control the exact padding
                windowInsets = WindowInsets(0, 0, 0, 25)
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
                        icon = { /* Add Icon */ }
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
            composable(PulseRoutes.FRAGMENT_B) {
                // ScreenB()
            }
            composable(PulseRoutes.FRAGMENT_C) {
                // ScreenC()
            }
            composable(PulseRoutes.FRAGMENT_D) {
                // ScreenD()
            }
            composable(PulseRoutes.FRAGMENT_E) {
                // ScreenE()
            }
        }
    }
}