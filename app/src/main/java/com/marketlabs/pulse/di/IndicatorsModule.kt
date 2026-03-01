package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.indicators.IndicatorsRepository
import com.marketlabs.pulse.core.indicators.IndicatorsRepositoryImpl
import com.marketlabs.pulse.network.store.indicators.RemoteIndicatorsDataSource
import com.marketlabs.pulse.network.store.indicators.RemoteIndicatorsDataSourceImpl
import com.marketlabs.pulse.storage.store.indicators.LocalIndicatorsDataSource
import com.marketlabs.pulse.storage.store.indicators.LocalIndicatorsDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IndicatorsModule {

    @Provides
    @Singleton
    fun provideRemoteIndicatorsDataSource(
        remoteDataSourceImpl: RemoteIndicatorsDataSourceImpl
    ) : RemoteIndicatorsDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalIndicatorsDataSource(
        localDataSourceImpl: LocalIndicatorsDataSourceImpl
    ) : LocalIndicatorsDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun provideIndicatorsRepository(
        indicatorsRepositoryImpl: IndicatorsRepositoryImpl
    ) : IndicatorsRepository = indicatorsRepositoryImpl

}