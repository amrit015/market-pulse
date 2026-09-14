package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.insights.InsightsHistoryRepository
import com.marketlabs.pulse.core.insights.InsightsHistoryRepositoryImpl
import com.marketlabs.pulse.network.store.insights.RemoteInsightsHistoryDataSource
import com.marketlabs.pulse.network.store.insights.RemoteInsightsHistoryDataSourceImpl
import com.marketlabs.pulse.storage.store.insights.LocalInsightsHistoryDataSource
import com.marketlabs.pulse.storage.store.insights.LocalInsightsHistoryDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds the Posture/Positioning history domain's Remote/Local data sources and repository, matching `MetricHistoryModule`'s 3-provider shape. */
@Module
@InstallIn(SingletonComponent::class)
object InsightsHistoryModule {

    @Provides
    @Singleton
    fun provideRemoteInsightsHistoryDataSource(
        remoteDataSourceImpl: RemoteInsightsHistoryDataSourceImpl
    ): RemoteInsightsHistoryDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalInsightsHistoryDataSource(
        localDataSourceImpl: LocalInsightsHistoryDataSourceImpl
    ): LocalInsightsHistoryDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun provideInsightsHistoryRepository(
        insightsHistoryRepositoryImpl: InsightsHistoryRepositoryImpl
    ): InsightsHistoryRepository = insightsHistoryRepositoryImpl
}
