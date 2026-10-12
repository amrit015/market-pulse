package com.marketlabs.pulse.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.marketlabs.pulse.core.analytics.AnalyticsEvent
import com.marketlabs.pulse.core.analytics.LocalAnalyticsTracker

/**
 * Logs `sheet_open` once when the calling bottom sheet enters composition. The sheet composables
 * are only composed while visible, so entering composition is the open; a `LaunchedEffect(Unit)`
 * keeps recomposition from logging again.
 */
@Composable
fun TrackSheetOpen(sheet: AnalyticsEvent.Sheet) {
    val tracker = LocalAnalyticsTracker.current
    LaunchedEffect(Unit) {
        tracker.log(AnalyticsEvent.SheetOpen(sheet, tracker.currentScreen?.name ?: UNKNOWN_SCREEN))
    }
}

private const val UNKNOWN_SCREEN = "unknown"
