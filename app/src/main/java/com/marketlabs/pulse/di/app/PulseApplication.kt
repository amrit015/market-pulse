package com.marketlabs.pulse.di.app

import android.app.Application
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.marketlabs.pulse.BuildConfig
import dagger.hilt.android.HiltAndroidApp

/**
 * Base Application class for MarketLabs Pulse.
 * This class triggers Hilt's code generation, including the base class
 * that serves as the application-level dependency container.
 */
@HiltAndroidApp
class PulseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeFirebaseAppCheck()
    }

    private fun initializeFirebaseAppCheck() {
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()

        if (BuildConfig.DEBUG) {
           Log.d("PulseAppCheck", "Mode: Debug Build (DebugProvider)")
            // 🛠️ Use Debug Provider for Emulators/Local Dev
            // 🔒 Define a static secret for local dev environment
            // Store this in local.properties and access via BuildConfig
            val staticDebugSecret = BuildConfig.DEBUG_SECRET_UUID

            // This keeps dev environments stable
            System.setProperty("firebase.appcheck.debug.token", staticDebugSecret)

            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            Log.d("PulseAppCheck", "Mode: DEVICE (PlayIntegrity)")
            // 🚀 Use Play Integrity for Production
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