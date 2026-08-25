package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.intraday.IntradayRepository
import com.marketlabs.pulse.core.intraday.IntradayRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * A single provider, not the standard 3 (`StockModule`'s remote/local/repository shape) --
 * `IntradayRepository` has no Local/Room half (see the interface's doc comment on why this
 * domain skips Room entirely), just a Retrofit-backed poll loop. Still `@Provides`-in-`object`,
 * `Impl → Interface`, per this project's DI convention.
 */
@Module
@InstallIn(SingletonComponent::class)
object IntradayModule {

    @Provides
    @Singleton
    fun provideIntradayRepository(
        intradayRepositoryImpl: IntradayRepositoryImpl
    ): IntradayRepository = intradayRepositoryImpl
}
