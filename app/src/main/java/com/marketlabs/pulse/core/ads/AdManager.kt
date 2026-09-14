package com.marketlabs.pulse.core.ads

import android.content.Context
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.flow.StateFlow

/**
 * High-level advertising coordinator for Market Pulse.
 *
 * Encapsulates MobileAds initialization, freemium / ad-free subscription state,
 * and lifecycle-aware native ad loading.
 */
interface AdManager {

    /**
     * Emits whether the user is on an ad-free tier (e.g., active Pro subscriber).
     * When true, ad loaders suppress all network calls and UI components hide ads entirely.
     */
    val isAdFree: StateFlow<Boolean>

    /**
     * Toggles or updates ad-free status (e.g. from Google Play Billing or dev/debug testing).
     */
    fun setAdFree(adFree: Boolean)

    /**
     * Emits true once the Google Mobile Ads SDK has finished initialization.
     */
    val isInitialized: StateFlow<Boolean>

    /**
     * Initializes the Google Mobile Ads SDK asynchronously. Safe to call idempotently.
     */
    fun initialize(context: Context)

    /**
     * Requests a NativeAd if ads are enabled and the user is not ad-free.
     *
     * @param context Calling context (typically Activity or Application)
     * @param adUnitId Target ad unit ID, defaulting to [AdConfig.activeNativeAdUnitId]
     * @param onAdLoaded Callback invoked on main thread when the native ad is ready
     * @param onAdFailed Callback invoked if the ad fails to load, returning an error message
     */
    fun loadNativeAd(
        context: Context,
        adUnitId: String = AdConfig.activeNativeAdUnitId,
        onAdLoaded: (NativeAd) -> Unit,
        onAdFailed: (String) -> Unit = {}
    )
}
