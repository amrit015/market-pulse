package com.marketlabs.pulse.core.analytics

/** Records nothing; the default for previews and any composition outside the app's root. */
object NoOpAnalyticsTracker : AnalyticsTracker {
    override val currentScreen: ScreenTarget? = null
    override fun screen(target: ScreenTarget, params: Map<String, Any>) = Unit
    override fun log(event: AnalyticsEvent) = Unit
    override fun setUserProperty(prop: UserProperty, value: String?) = Unit
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}
