package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.riskRadar.RiskRadarRepository
import com.marketlabs.pulse.core.riskRadar.RiskRadarRepositoryImpl
import com.marketlabs.pulse.network.store.riskRadar.RemoteRiskRadarDataSource
import com.marketlabs.pulse.network.store.riskRadar.RemoteRiskRadarDataSourceImpl
import com.marketlabs.pulse.storage.repository.riskRadar.LocalRiskRadarDataSource
import com.marketlabs.pulse.storage.repository.riskRadar.LocalRiskRadarDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RiskRadarModule {

    @Provides
    @Singleton
    fun provideRemoteRiskRadarDataSource(
        remoteDataSourceImpl: RemoteRiskRadarDataSourceImpl
    ): RemoteRiskRadarDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalRiskRadarDataSource(
        localDataSourceImpl: LocalRiskRadarDataSourceImpl
    ): LocalRiskRadarDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun provideRiskRadarRepository(
        marketSummaryRepositoryImpl: RiskRadarRepositoryImpl
    ): RiskRadarRepository = marketSummaryRepositoryImpl

}