package com.marketlabs.pulse.core.ads

/**
 * Centralized configuration for Google AdMob within Market Pulse.
 *
 * Provides Google's official sample test unit IDs by default to protect against invalid click
 * penalties during development, while offering a single pluggable location for production IDs
 * once an AdMob account is created.
 */
object AdConfig {

    /**
     * Google's official sample AdMob App ID.
     */
    const val SAMPLE_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    /**
     * Google's official sample Native Advanced Ad Unit ID for Android.
     */
    const val SAMPLE_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    /**
     * Production Native Ad Unit ID. Replace this when your real AdMob account & ad unit is set up.
     */
    var productionNativeAdUnitId: String? = null

    /**
     * Resolves the active native ad unit ID. Prefers production if provided, otherwise sample test ID.
     */
    val activeNativeAdUnitId: String
        get() = productionNativeAdUnitId?.takeIf { it.isNotBlank() } ?: SAMPLE_NATIVE_AD_UNIT_ID
}
