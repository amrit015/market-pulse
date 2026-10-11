package com.marketlabs.pulse.ui.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.marketlabs.pulse.core.analytics.LocalAnalyticsTracker
import com.marketlabs.pulse.core.analytics.ScreenTarget

/**
 * Screen names that were showing when the Activity was torn down for a configuration change. The
 * recreated Activity resumes the same screen, which isn't a new view, so the first resume per name
 * is swallowed. Main thread only.
 */
private val loggedBeforeRecreation = mutableSetOf<String>()

/**
 * Logs `screen_view` when the screen resumes, and again whenever [keys] change (the selected tab).
 * Fires from an effect, never from composition, so recomposition doesn't double-count; rotation
 * is filtered out via [loggedBeforeRecreation].
 */
@Composable
fun TrackScreen(
    target: ScreenTarget,
    vararg keys: Any?,
    params: Map<String, Any> = emptyMap()
) {
    val tracker = LocalAnalyticsTracker.current
    val activity = LocalContext.current.findActivity()
    val currentTarget = rememberUpdatedState(target)
    val currentParams = rememberUpdatedState(params)
    LifecycleResumeEffect(tracker, *keys) {
        val name = currentTarget.value.name
        if (!loggedBeforeRecreation.remove(name)) {
            tracker.screen(currentTarget.value, currentParams.value)
        }
        onPauseOrDispose {
            if (activity?.isChangingConfigurations == true) loggedBeforeRecreation.add(name)
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
