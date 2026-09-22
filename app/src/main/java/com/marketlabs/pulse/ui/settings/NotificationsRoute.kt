package com.marketlabs.pulse.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.ui.components.rememberNotificationPermission

/**
 * Stateful wrapper for [NotificationsScreen]. The platform side of notification permission lives in
 * `rememberNotificationPermission` (it needs an Activity/Context, so not the ViewModel).
 *
 * Turning a toggle on keeps the preference and subscribes to its topics regardless of what the
 * user answers to the system dialog: the choice is theirs, and subscribing needs no permission.
 * The dialog is requested only from that first toggle-on (never at cold start). Once denied
 * permanently the system returns "denied" immediately without showing anything, so the same request
 * doubles as the permanently-denied path: the hint card, with its settings link, is what stays on
 * screen; it clears the moment the user grants notifications in system settings and comes back.
 */
@Composable
fun NotificationsRoute(
    onNavigateUp: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permission = rememberNotificationPermission()

    NotificationsScreen(
        uiState = uiState,
        showPermissionHint = uiState.anyEnabled && !permission.allowed,
        onToggle = { preference, enabled ->
            viewModel.setEnabled(preference, enabled)
            // Below Android 13 there is no runtime permission to request; the hint's settings link
            // is the only remedy there.
            if (enabled) permission.requestIfNeeded()
        },
        onOpenNotificationSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        },
        onNavigateUp = onNavigateUp
    )
}

