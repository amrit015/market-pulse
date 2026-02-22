package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.news.NewsRepository
import com.marketlabs.pulse.core.news.NewsRepositoryImpl
import com.marketlabs.pulse.network.store.news.RemoteNewsDataSource
import com.marketlabs.pulse.network.store.news.RemoteNewsDataSourceImpl
import com.marketlabs.pulse.storage.repository.news.LocalNewsDataSource
import com.marketlabs.pulse.storage.repository.news.LocalNewsDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NewsModule {

    @Provides
    @Singleton
    fun provideRemoteNewsDataSource(
        remoteDataSourceImpl: RemoteNewsDataSourceImpl
    ): RemoteNewsDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalNewsDataSource(
        localDataSourceImpl: LocalNewsDataSourceImpl
    ): LocalNewsDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun provideNewsRepository(
        marketSummaryRepositoryImpl: NewsRepositoryImpl
    ): NewsRepository = marketSummaryRepositoryImpl
}