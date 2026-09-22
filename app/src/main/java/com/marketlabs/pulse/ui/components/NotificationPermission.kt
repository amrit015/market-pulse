package com.marketlabs.pulse.ui.components

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/**
 * Whether this app may currently post notifications, and how to ask for that.
 *
 * [allowed] covers both the Android 13+ runtime permission and the per-app "notifications off"
 * switch every version has, so it is the single check for "would a notification actually show". It
 * is re-read on every resume, so it flips the moment the user changes it in system settings and
 * comes back.
 */
@Stable
class NotificationPermission internal constructor(
    val allowed: Boolean,
    private val launchRequest: () -> Unit
) {
    /**
     * Shows the system permission dialog when it can help: notifications are blocked and the device
     * is on Android 13+, where the permission is a runtime one. Below that there is nothing to
     * request. Once denied permanently the system answers "denied" immediately without a dialog, so
     * calling this again is harmless.
     */
    fun requestIfNeeded() {
        if (!allowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) launchRequest()
    }
}

@Composable
fun rememberNotificationPermission(): NotificationPermission {
    val context = LocalContext.current
    var allowed by remember { mutableStateOf(areNotificationsAllowed(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { allowed = areNotificationsAllowed(context) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        allowed = areNotificationsAllowed(context)
    }
    return NotificationPermission(
        allowed = allowed,
        launchRequest = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
    )
}

private fun areNotificationsAllowed(context: Context): Boolean =
    NotificationManagerCompat.from(context).areNotificationsEnabled()
