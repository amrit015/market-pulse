package com.marketlabs.pulse.core.analytics

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Provided once in `MainActivity` so any composable can reach the tracker without a ViewModel hop.
 * Defaults to a no-op so `@Preview`s render without a Hilt graph.
 */
val LocalAnalyticsTracker = staticCompositionLocalOf<AnalyticsTracker> { NoOpAnalyticsTracker }
