package com.marketlabs.pulse.data.privacy

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** `@Provides`-in-`object` per this repo's DI convention — no `@Binds`. */
@Module
@InstallIn(SingletonComponent::class)
object AnalyticsConsentModule {

    @Provides
    @Singleton
    fun provideAnalyticsConsentRepository(
        impl: AnalyticsConsentRepositoryImpl
    ): AnalyticsConsentRepository = impl
}
