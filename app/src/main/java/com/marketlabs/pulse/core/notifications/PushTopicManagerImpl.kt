package com.marketlabs.pulse.core.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.marketlabs.pulse.data.notifications.NotificationPreference
import com.marketlabs.pulse.data.notifications.NotificationPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PushTopicManagerImpl @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val preferencesRepository: NotificationPreferencesRepository
) : PushTopicManager {

    override fun apply(preference: NotificationPreference, enabled: Boolean) {
        NotificationTopics.topicsFor(preference).forEach { topic ->
            val task = if (enabled) {
                firebaseMessaging.subscribeToTopic(topic)
            } else {
                firebaseMessaging.unsubscribeFromTopic(topic)
            }
            // A failure isn't retried here: the next app start's reconcile() re-issues it.
            task.addOnFailureListener { error ->
                Log.w(TAG, "${if (enabled) "subscribe" else "unsubscribe"} failed for $topic", error)
            }
        }
    }

    override suspend fun reconcile() {
        val enabled = preferencesRepository.enabledPreferences.first()
        NotificationPreference.entries.forEach { apply(it, it in enabled) }
    }

    private companion object {
        const val TAG = "PushTopicManager"
    }
}
