package com.marketlabs.pulse.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.marketlabs.pulse.core.analytics.AnalyticsEvent
import com.marketlabs.pulse.core.analytics.LocalAnalyticsTracker

/** Logs `tutorial_begin` once when a deck or article screen is first shown. */
@Composable
fun TrackTutorialBegin(key: AnalyticsEvent.TutorialKey) {
    val tracker = LocalAnalyticsTracker.current
    LaunchedEffect(Unit) { tracker.log(AnalyticsEvent.TutorialBegin(key)) }
}

/**
 * A callback that logs `tutorial_complete` the first time it's invoked. The carousel reports its
 * last card every time the reader settles on it, so swiping back and forth must not count twice.
 */
@Composable
fun rememberTutorialCompleteLogger(key: AnalyticsEvent.TutorialKey): () -> Unit {
    val tracker = LocalAnalyticsTracker.current
    return remember(tracker) {
        var logged = false
        {
            if (!logged) {
                logged = true
                tracker.log(AnalyticsEvent.TutorialComplete(key))
            }
        }
    }
}
