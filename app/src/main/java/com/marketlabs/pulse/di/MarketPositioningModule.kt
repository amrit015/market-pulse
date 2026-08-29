package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.positioning.MarketPositioningRepository
import com.marketlabs.pulse.core.positioning.MarketPositioningRepositoryImpl
import com.marketlabs.pulse.network.store.positioning.RemoteMarketPositioningDataSource
import com.marketlabs.pulse.network.store.positioning.RemoteMarketPositioningDataSourceImpl
import com.marketlabs.pulse.storage.store.positioning.LocalMarketPositioningDataSource
import com.marketlabs.pulse.storage.store.positioning.LocalMarketPositioningDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MarketPositioningModule {

    @Provides
    @Singleton
    fun provideRemoteMarketPositioningDataSource(
        remoteDataSourceImpl: RemoteMarketPositioningDataSourceImpl
    ): RemoteMarketPositioningDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalMarketPositioningDataSource(
        localDataSourceImpl: LocalMarketPositioningDataSourceImpl
    ): LocalMarketPositioningDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun provideMarketPositioningRepository(
        marketPositioningRepositoryImpl: MarketPositioningRepositoryImpl
    ): MarketPositioningRepository = marketPositioningRepositoryImpl
}
