package com.marketlabs.pulse.ui.navigation

import android.content.Intent
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Maps a tapped push notification to an in-app route. A push's `data` payload arrives as extras on
 * the launch intent (cold start in `onCreate`, warm start in `onNewIntent`). Only `data.screen`
 * decides where to go; the notification's title and body are server-owned copy that varies (the
 * summary is titled "Weekend Summary" on weekends) and is never inspected.
 */
object PushNavigation {

    private const val EXTRA_SCREEN = "screen"
    private const val EXTRA_TYPE = "type"
    private const val EXTRA_KEY = "key"
    private const val EXTRA_FCM_MESSAGE_ID = "google.message_id"

    // Keep in step with the backend's `data.screen` values.
    private const val SCREEN_MARKET_SUMMARY = "market_summary"
    private const val SCREEN_STOCKS = "stocks"

    /**
     * The route to open for [intent], or `null` when the intent didn't come from a push tap (an
     * ordinary launcher start must not navigate). A push with a missing or unrecognized
     * `data.screen` opens the app home rather than being ignored.
     */
    fun routeFor(intent: Intent?): String? {
        val extras = intent?.extras ?: return null
        val isPush = extras.containsKey(EXTRA_SCREEN) ||
            extras.containsKey(EXTRA_TYPE) ||
            extras.containsKey(EXTRA_FCM_MESSAGE_ID)
        if (!isPush) return null
        return routeForScreen(extras.getString(EXTRA_SCREEN))
    }

    /**
     * What identifies a tapped push for analytics: its `data.type`, `data.key` and the `data.screen`
     * it targeted. `null` for anything that isn't a push tap, mirroring [routeFor]. [key] is `null`
     * (not empty) for older pushes that carry none.
     */
    fun pushInfoFor(intent: Intent?): PushInfo? {
        val extras = intent?.extras ?: return null
        val isPush = extras.containsKey(EXTRA_SCREEN) ||
            extras.containsKey(EXTRA_TYPE) ||
            extras.containsKey(EXTRA_FCM_MESSAGE_ID)
        return pushInfoOf(
            isPush = isPush,
            type = extras.getString(EXTRA_TYPE),
            screen = extras.getString(EXTRA_SCREEN),
            key = extras.getString(EXTRA_KEY)
        )
    }

    internal fun pushInfoOf(isPush: Boolean, type: String?, screen: String?, key: String?): PushInfo? =
        if (isPush) PushInfo(type = type, screen = screen, key = key?.takeIf { it.isNotEmpty() }) else null

    internal fun routeForScreen(screen: String?): String = when (screen) {
        SCREEN_MARKET_SUMMARY -> PulseRoutes.MARKET_SUMMARY
        SCREEN_STOCKS -> PulseRoutes.MARKET_ANALYSIS
        else -> PulseRoutes.MARKET_OVERVIEW
    }
}

/** A tapped push's identifying `data` fields; see [PushNavigation.pushInfoFor]. */
data class PushInfo(val type: String?, val screen: String?, val key: String?)

/**
 * Switches to a bottom-nav tab exactly as a tap on the floating nav does: collapse back to the start
 * destination, keeping each tab's saved state, so the back stack never accumulates tabs and an
 * open pushed screen (e.g. Stock Detail) is popped.
 */
fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
