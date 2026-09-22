package com.marketlabs.pulse.core.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives pushes while the app is in the foreground — with the app in the background or closed,
 * the system tray displays a notification-payload message by itself and this class isn't involved.
 * A foreground push is deliberately not re-posted: the screen the user is looking at is already
 * live. Title and body are server-owned and never parsed; `data.type` is logged only for
 * diagnostics, and tap routing reads `data.screen` from the launch intent instead.
 */
class PulseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "foreground push suppressed, type=${message.data["type"]} screen=${message.data["screen"]}")
    }

    // Topic subscriptions are keyed to the app instance, not to a stored token, so a refreshed
    // token needs no client handling in this version.
    override fun onNewToken(token: String) {
        Log.d(TAG, "FCM token refreshed")
    }

    private companion object {
        const val TAG = "PulseMessaging"
    }
}
