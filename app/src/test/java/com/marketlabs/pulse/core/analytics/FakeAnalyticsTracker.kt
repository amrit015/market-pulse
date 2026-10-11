package com.marketlabs.pulse.core.analytics

class FakeAnalyticsTracker : AnalyticsTracker {
    val events = mutableListOf<AnalyticsEvent>()
    val collectionCalls = mutableListOf<Boolean>()

    override var currentScreen: ScreenTarget? = null

    override fun screen(target: ScreenTarget, params: Map<String, Any>) {
        currentScreen = target
    }

    override fun log(event: AnalyticsEvent) {
        events += event
    }

    override fun setUserProperty(prop: UserProperty, value: String?) = Unit

    override fun setCollectionEnabled(enabled: Boolean) {
        collectionCalls += enabled
    }
}
