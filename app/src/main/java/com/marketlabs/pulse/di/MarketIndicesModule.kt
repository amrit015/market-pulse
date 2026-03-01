package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.marketIndex.MarketIndexRepository
import com.marketlabs.pulse.core.marketIndex.MarketIndexRepositoryImpl
import com.marketlabs.pulse.network.store.marketIndex.RemoteMarketDataSource
import com.marketlabs.pulse.network.store.marketIndex.RemoteMarketDataSourceImpl
import com.marketlabs.pulse.storage.store.marketIndex.LocalMarketDataSource
import com.marketlabs.pulse.storage.store.marketIndex.LocalMarketDataSourceImpl
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