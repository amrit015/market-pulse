package com.marketlabs.pulse.di.app

import android.app.Application
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.marketlabs.pulse.BuildConfig
import com.marketlabs.pulse.core.ads.AdManager
import com.marketlabs.pulse.core.notifications.NotificationChannels
import com.marketlabs.pulse.core.notifications.PushTopicManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Base Application class for MarketLabs Pulse.
 * This class triggers Hilt's code generation, including the base class
 * that serves as the application-level dependency container.
 */
@HiltAndroidApp
class PulseApplication : Application() {

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var pushTopicManager: PushTopicManager

    // Process-lifetime scope for the one-shot subscription reconcile below.
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        initializeFirebaseAppCheck()
        adManager.initialize(this)
        // Channels first: a push can wake this process before any Activity exists, and it must
        // find its channel already created.
        NotificationChannels.createAll(this)
        applicationScope.launch { pushTopicManager.reconcile() }
    }

    private fun initializeFirebaseAppCheck() {
        // 1. SET THE PROPERTY FIRST
        if (BuildConfig.DEBUG) {
            val staticDebugSecret = BuildConfig.DEBUG_SECRET_UUID
            System.setProperty("firebase.appcheck.debug.secret", staticDebugSecret)
            Log.d("PulseAppCheck", "Mode: Debug Build - Secret Set: $staticDebugSecret")
        }

        // 2. NOW INITIALIZE FIREBASE
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()

        // 3. INSTALL THE PROVIDER
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            Log.d("PulseAppCheck", "Mode: DEVICE (PlayIntegrity)")
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
    }

    // Helper to detect if the app is running on an emulator
    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
    }

    // Check for your specific physical phone model
    private fun isMyPhysicalPhone(): Boolean {
        // Replace with your actual phone brand/model found in Build.BRAND/Build.MODEL
        return Build.MODEL.contains("Pixel") || Build.MANUFACTURER.contains("Samsung")
    }
}