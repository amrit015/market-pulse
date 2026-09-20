package com.marketlabs.pulse.data.favorites

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** `@Provides`-in-`object` per this repo's DI convention -- no `@Binds`. */
@Module
@InstallIn(SingletonComponent::class)
object FavoriteStocksModule {

    @Provides
    @Singleton
    fun provideFavoriteStocksRepository(
        favoriteStocksRepositoryImpl: FavoriteStocksRepositoryImpl
    ): FavoriteStocksRepository = favoriteStocksRepositoryImpl
}
