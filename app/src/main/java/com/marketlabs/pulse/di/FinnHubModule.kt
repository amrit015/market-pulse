package com.marketlabs.pulse.di

import com.marketlabs.pulse.BuildConfig
import com.marketlabs.pulse.network.api.FinnHubService
import com.marketlabs.pulse.network.interceptor.FinnHubAuthInterceptor
import com.marketlabs.pulse.utils.Constants.FINNHUB_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object FinnHubModule {

    @Provides
    @Singleton
    fun provideFinnHubService(): FinnHubService {
        // Create an auth interceptor to inject the key from BuildConfig
        val authInterceptor = FinnHubAuthInterceptor(BuildConfig.FINNHUB_KEY)

        // Create logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Build the OkHttp client
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // adds key
            .addInterceptor(loggingInterceptor) // logs result
            .build()

        // build retrofit
        return Retrofit.Builder()
            .baseUrl(FINNHUB_BASE_URL)
            .client(client)
            .addConverterFactory(
                MoshiConverterFactory.create()
            )
            .build()
            .create(FinnHubService::class.java)
    }
}