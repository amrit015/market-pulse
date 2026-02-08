package com.marketlabs.pulse.di.app

import android.app.Application
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
            // 🛠️ Use Debug Provider for Emulators/Local Dev
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            // 🚀 Use Play Integrity for Production
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
    }
}