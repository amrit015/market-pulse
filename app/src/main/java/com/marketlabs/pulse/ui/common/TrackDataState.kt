package com.marketlabs.pulse.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.marketlabs.pulse.core.analytics.AnalyticsEvent
import com.marketlabs.pulse.core.analytics.LocalAnalyticsTracker
import java.util.Locale

/**
 * Logs `data_state_shown` when a shared error or empty state enters composition, keyed by [state]
 * so a switch between the two logs again but recomposition doesn't. The screen is whatever
 * `TrackScreen` last reported; the domain is that screen's route group, since these states are
 * shared components with no domain of their own.
 */
@Composable
fun TrackDataState(state: AnalyticsEvent.DataState) {
    val tracker = LocalAnalyticsTracker.current
    LaunchedEffect(state) {
        val screen = tracker.currentScreen
        tracker.log(
            AnalyticsEvent.DataStateShown(
                state = state,
                screenName = screen?.name ?: UNKNOWN,
                domain = screen?.screenClass?.lowercase(Locale.ROOT) ?: UNKNOWN
            )
        )
    }
}

private const val UNKNOWN = "unknown"
