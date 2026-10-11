package com.marketlabs.pulse.core.analytics

import com.marketlabs.pulse.BuildConfig

/** Whether analytics may actually record, given the user's choice and the build type. */
object AnalyticsCollection {

    /**
     * Debug builds stay silent unless `ANALYTICS_IN_DEBUG` is flipped locally, so developer runs
     * never mix into production numbers.
     */
    fun effective(userEnabled: Boolean): Boolean =
        userEnabled && (!BuildConfig.DEBUG || BuildConfig.ANALYTICS_IN_DEBUG)
}
