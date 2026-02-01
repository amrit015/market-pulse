package com.marketlabs.pulse.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Base Application class for MarketLabs Pulse.
 * This class triggers Hilt's code generation, including the base class
 * that serves as the application-level dependency container.
 */
@HiltAndroidApp
class PulseApplication : Application()