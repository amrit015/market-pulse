package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.posture.MarketPostureRepository
import com.marketlabs.pulse.core.posture.MarketPostureRepositoryImpl
import com.marketlabs.pulse.network.store.posture.RemoteMarketPostureDataSource
import com.marketlabs.pulse.network.store.posture.RemoteMarketPostureDataSourceImpl
import com.marketlabs.pulse.storage.store.posture.LocalMarketPostureDataSource
import com.marketlabs.pulse.storage.store.posture.LocalMarketPostureDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MarketPostureModule {

    @Provides
    @Singleton
    fun provideRemoteMarketPostureDataSource(
        remoteDataSourceImpl: RemoteMarketPostureDataSourceImpl
    ): RemoteMarketPostureDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalMarketPostureDataSource(
        localDataSourceImpl: LocalMarketPostureDataSourceImpl
    ): LocalMarketPostureDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun provideMarketPostureRepository(
        marketPostureRepositoryImpl: MarketPostureRepositoryImpl
    ): MarketPostureRepository = marketPostureRepositoryImpl
}