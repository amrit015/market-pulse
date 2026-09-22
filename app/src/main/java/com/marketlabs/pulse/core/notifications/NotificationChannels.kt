package com.marketlabs.pulse.core.notifications

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.marketlabs.pulse.R

/**
 * The backend names these ids in each message's `android.notification.channelId`, so they must match
 * exactly: a push naming a channel that doesn't exist silently lands on FCM's fallback channel and
 * the user loses per-channel control in system settings.
 */
object NotificationChannels {
    const val DAILY_SUMMARY = "daily_summary"
    const val STOCK_UPDATES = "stock_updates"

    /**
     * Idempotent — re-creating an existing channel is a no-op (a channel's name and importance can't
     * be changed after first creation except by the user), so this runs on every process start, well
     * before any push can be delivered.
     */
    fun createAll(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(DAILY_SUMMARY, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.notification_channel_daily_summary_name))
                .setDescription(context.getString(R.string.notification_channel_daily_summary_description))
                .build()
        )
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(STOCK_UPDATES, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.notification_channel_stock_updates_name))
                .setDescription(context.getString(R.string.notification_channel_stock_updates_description))
                .build()
        )
    }
}
