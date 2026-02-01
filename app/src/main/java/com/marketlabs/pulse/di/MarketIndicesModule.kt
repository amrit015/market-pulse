package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.MarketIndexRepository
import com.marketlabs.pulse.core.MarketIndexRepositoryImpl
import com.marketlabs.pulse.network.store.RemoteMarketDataSource
import com.marketlabs.pulse.network.store.RemoteMarketDataSourceImpl
import com.marketlabs.pulse.storage.repository.LocalMarketDataSource
import com.marketlabs.pulse.storage.repository.LocalMarketDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MarketIndicesModule {

    @Provides
    @Singleton
    fun provideRemoteDataSource(
        remoteMarketDataSourceImpl: RemoteMarketDataSourceImpl
    ): RemoteMarketDataSource = remoteMarketDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalDataSource(
        localMarketDataSourceImpl: LocalMarketDataSourceImpl
    ): LocalMarketDataSource = localMarketDataSourceImpl

    @Provides
    @Singleton
    fun provideMarketIndexRepository(
        marketIndexRepositoryImpl: MarketIndexRepositoryImpl
    ): MarketIndexRepository = marketIndexRepositoryImpl
}