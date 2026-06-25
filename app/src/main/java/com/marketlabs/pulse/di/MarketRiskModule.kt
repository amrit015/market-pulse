package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.marketRisk.MarketRiskRepository
import com.marketlabs.pulse.core.marketRisk.MarketRiskRepositoryImpl
import com.marketlabs.pulse.network.store.marketRisk.RemoteMarketRiskDataSource
import com.marketlabs.pulse.network.store.marketRisk.RemoteMarketRiskDataSourceImpl
import com.marketlabs.pulse.storage.store.marketRisk.LocalMarketRiskDataSource
import com.marketlabs.pulse.storage.store.marketRisk.LocalMarketRiskDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MarketRiskModule {

    @Provides
    @Singleton
    fun provideRemoteMarketRiskDataSource(
        remoteDataSourceImpl: RemoteMarketRiskDataSourceImpl
    ): RemoteMarketRiskDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalMarketRiskDataSource(
        localDataSourceImpl: LocalMarketRiskDataSourceImpl
    ): LocalMarketRiskDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun provideMarketRiskRepository(
        marketSummaryRepositoryImpl: MarketRiskRepositoryImpl
    ): MarketRiskRepository = marketSummaryRepositoryImpl

}