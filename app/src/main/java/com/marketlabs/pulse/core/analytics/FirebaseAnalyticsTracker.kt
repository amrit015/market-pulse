package com.marketlabs.pulse.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** The only class that imports Firebase Analytics. */
@Singleton
class FirebaseAnalyticsTracker @Inject constructor(
    @ApplicationContext context: Context
) : AnalyticsTracker {

    private val analytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics get() = FirebaseCrashlytics.getInstance()

    // Null until the first call, so the startup sync of an already-off setting doesn't wipe the
    // instance id on every launch; only a real on -> off change resets.
    private var lastEnabled: Boolean? = null

    @Volatile
    override var currentScreen: ScreenTarget? = null
        private set

    override fun screen(target: ScreenTarget, params: Map<String, Any>) {
        currentScreen = target
        val bundle = params.toBundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, target.name)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, target.screenClass)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        // So every crash report shows where in the app it happened.
        crashlytics.setCustomKey(CRASHLYTICS_SCREEN_KEY, target.name)
    }

    override fun log(event: AnalyticsEvent) {
        analytics.logEvent(event.name, event.params.toBundle())
    }

    override fun setUserProperty(prop: UserProperty, value: String?) {
        analytics.setUserProperty(prop.key, value)
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
        // Clears the local instance's analytics state, so turning sharing off doesn't leave
        // already-collected data waiting to upload.
        if (!enabled && lastEnabled == true) analytics.resetAnalyticsData()
        lastEnabled = enabled
    }

    private fun Map<String, Any>.toBundle(): Bundle = Bundle().also { bundle ->
        forEach { (key, value) ->
            when (value) {
                is Long -> bundle.putLong(key, value)
                is String -> bundle.putString(key, value)
                else -> error("Analytics param '$key' must be String or Long")
            }
        }
    }

    private companion object {
        const val CRASHLYTICS_SCREEN_KEY = "screen_name"
    }
}
