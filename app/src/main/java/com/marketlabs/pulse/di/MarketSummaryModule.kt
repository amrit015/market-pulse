package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.marketSummary.MarketSummaryRepository
import com.marketlabs.pulse.core.marketSummary.MarketSummaryRepositoryImpl
import com.marketlabs.pulse.network.store.summary.RemoteMarketSummaryDataSource
import com.marketlabs.pulse.network.store.summary.RemoteMarketSummaryDataSourceImpl
import com.marketlabs.pulse.storage.repository.marketIndex.LocalMarketDataSource
import com.marketlabs.pulse.storage.repository.marketIndex.LocalMarketDataSourceImpl
import com.marketlabs.pulse.storage.repository.summary.LocalMarketSummaryDataSource
import com.marketlabs.pulse.storage.repository.summary.LocalMarketSummaryDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MarketSummaryModule {

    @Provides
    @Singleton
    fun provideRemoteMarketSummaryDataSource(
        remoteDataSourceImpl: RemoteMarketSummaryDataSourceImpl
    ): RemoteMarketSummaryDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalMarketSummaryDataSource(
        localDataSourceImpl: LocalMarketSummaryDataSourceImpl
    ): LocalMarketSummaryDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun provideMarketSummaryRepository(
        marketSummaryRepositoryImpl: MarketSummaryRepositoryImpl
    ): MarketSummaryRepository = marketSummaryRepositoryImpl

}