package com.marketlabs.pulse.core.analytics

/**
 * The only analytics surface the rest of the app sees. Firebase stays behind this so event names
 * live in one place, ViewModels can be tested with a fake, and swapping tools touches one module.
 */
interface AnalyticsTracker {

    /**
     * The screen last passed to [screen], so events fired from shared components (a bottom
     * sheet, an error state) can say which screen they appeared on without every caller threading
     * the name through. `null` before the first screen.
     */
    val currentScreen: ScreenTarget?

    fun screen(target: ScreenTarget, params: Map<String, Any> = emptyMap())

    fun log(event: AnalyticsEvent)

    /** A `null` [value] clears the property. */
    fun setUserProperty(prop: UserProperty, value: String?)

    /** Combines the user's choice with the build type; see [AnalyticsCollection]. */
    fun setCollectionEnabled(enabled: Boolean)
}
