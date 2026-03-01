package com.marketlabs.pulse.network.retrofit

import com.marketlabs.pulse.BuildConfig
import com.marketlabs.pulse.network.api.finnhub.FinnHubService
import com.marketlabs.pulse.network.interceptor.FinnHubAuthInterceptor
import com.marketlabs.pulse.network.websockets.FinnhubWebSocketClient
import com.marketlabs.pulse.utils.Constants
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FinnHubApiModule {

    // ==========================================
    // 🌐 1. FINNHUB REST API (HTTP) DEPENDENCIES
    // ==========================================

    @Provides
    @Singleton
    @Named("FinnHubRestClient")
    fun provideFinnHubRestOkHttpClient(): OkHttpClient {
        val authInterceptor = FinnHubAuthInterceptor(BuildConfig.FINNHUB_KEY)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideFinnHubService(
        @Named("FinnHubRestClient") client: OkHttpClient
    ): FinnHubService {
        return Retrofit.Builder()
            .baseUrl(Constants.FINNHUB_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(FinnHubService::class.java)
    }

    // ==========================================
    // 🔌 2. FINNHUB WEBSOCKET DEPENDENCIES
    // ==========================================

    @Provides
    @Singleton
    @Named("FinnHubWebSocketClient")
    fun provideFinnHubWebSocketOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC // Less noisy for WebSockets
        }

        return OkHttpClient.Builder()
            // 💡 CRITICAL: Keeps the connection alive so it doesn't silently timeout
            .pingInterval(15, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideFinnHubWebSocketClient(
        @Named("FinnHubWebSocketClient") okHttpClient: OkHttpClient,
        moshi: Moshi
    ): FinnhubWebSocketClient {
        return FinnhubWebSocketClient(okHttpClient, moshi)
    }
}