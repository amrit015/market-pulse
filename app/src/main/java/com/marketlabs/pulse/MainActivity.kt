package com.marketlabs.pulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marketlabs.pulse.ui.components.AppTopBar
import com.marketlabs.pulse.ui.components.FloatingBottomNav
import com.marketlabs.pulse.ui.navigation.PulseNavGraph
import com.marketlabs.pulse.ui.navigation.PulseRoutes
import com.marketlabs.pulse.ui.navigation.bottomNavItems
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.data.theme.ThemeRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 💡 THOUGHT PROCESS:
 * Rewritten for spec-20260809-theme-migration. Previously just `enableEdgeToEdge()` +
 * `MarketPulseTheme { PulseNavGraph() }` — the `Scaffold`, the global collapsing `AppTopBar`, and
 * the floating `FloatingBottomNav` now all live here instead of inside `PulseNavGraph`, since they
 * are app-wide chrome, not per-graph. `navController` is created here (not inside the graph) so it
 * can drive both the top bar's gear navigation and the floating nav's selected-tab highlighting.
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

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        AppTopBar(
                            scrollBehavior = scrollBehavior,
                            onSettingsClick = { navController.navigate(PulseRoutes.SETTINGS) }
                        )
                    },
                    bottomBar = {
                        // Settings is a pushed, full-screen destination reached from the gear —
                        // there is no "Settings" tab, so the floating nav has no place there.
                        if (currentRoute != PulseRoutes.SETTINGS) {
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
                    PulseNavGraph(
                        navController = navController,
                        scaffoldPadding = innerPadding
                    )
                }
            }
        }
    }
}
