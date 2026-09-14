package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.ads.AdManager
import com.marketlabs.pulse.core.ads.AdManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdModule {

    @Provides
    @Singleton
    fun provideAdManager(
        adManagerImpl: AdManagerImpl
    ): AdManager = adManagerImpl
}
