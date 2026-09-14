package com.marketlabs.pulse.core.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AdManager"

@Singleton
class AdManagerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : AdManager {

    private val _isAdFree = MutableStateFlow(false)
    override val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun setAdFree(adFree: Boolean) {
        _isAdFree.value = adFree
        Log.d(TAG, "Ad-Free status updated: $adFree")
    }

    override fun initialize(context: Context) {
        if (_isInitialized.value) return

        try {
            MobileAds.initialize(context) { initializationStatus ->
                _isInitialized.value = true
                Log.d(TAG, "MobileAds initialized: ${initializationStatus.adapterStatusMap.keys}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MobileAds", e)
        }
    }

    override fun loadNativeAd(
        context: Context,
        adUnitId: String,
        onAdLoaded: (NativeAd) -> Unit,
        onAdFailed: (String) -> Unit
    ) {
        if (_isAdFree.value) {
            Log.d(TAG, "Ad load skipped: User is on ad-free tier")
            return
        }

        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->
                mainHandler.post {
                    onAdLoaded(nativeAd)
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    val message = "Ad failed to load: [Code: ${adError.code}] ${adError.message}"
                    Log.w(TAG, message)
                    mainHandler.post {
                        onAdFailed(message)
                    }
                }

                override fun onAdClicked() {
                    Log.d(TAG, "Native ad clicked")
                }

                override fun onAdImpression() {
                    Log.d(TAG, "Native ad impression recorded")
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }
}
