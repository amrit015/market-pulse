package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.summary.SummaryRepository
import com.marketlabs.pulse.core.summary.SummaryRepositoryImpl
import com.marketlabs.pulse.network.store.summary.RemoteSummaryDataSource
import com.marketlabs.pulse.network.store.summary.RemoteSummaryDataSourceImpl
import com.marketlabs.pulse.storage.repository.summary.LocalSummaryDataSource
import com.marketlabs.pulse.storage.repository.summary.LocalSummaryDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SummaryModule {

    @Provides
    @Singleton
    fun provideRemoteMarketSummaryDataSource(
        remoteDataSourceImpl: RemoteSummaryDataSourceImpl
    ): RemoteSummaryDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalMarketSummaryDataSource(
        localDataSourceImpl: LocalSummaryDataSourceImpl
    ): LocalSummaryDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun provideMarketSummaryRepository(
        marketSummaryRepositoryImpl: SummaryRepositoryImpl
    ): SummaryRepository = marketSummaryRepositoryImpl

}