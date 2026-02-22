package com.marketlabs.pulse.network.retrofit

import com.marketlabs.pulse.network.api.MarketPulseApi
import com.marketlabs.pulse.network.interceptor.AppCheckInterceptor
import com.marketlabs.pulse.network.interceptor.HeaderLoggingInterceptor
import com.marketlabs.pulse.utils.Constants.MARKET_PULSE_BASE_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
object MarketPulseModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            // 💡 Required for Kotlin data classes (handling default values/nullability)
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    @Named("MarketPulseClient") // specific name to avoid conflict with FinnHub client
    fun provideMarketPulseOkHttpClient(
        appCheckInterceptor: AppCheckInterceptor, // Make sure this is injected
        headerLoggingInterceptor: HeaderLoggingInterceptor // The one we just added
    ): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            // TURN OFF app check for now
//            .addInterceptor(appCheckInterceptor) // Add the Firebase app check interceptor here
            .addInterceptor(headerLoggingInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideMarketPulseApi(
        @Named("MarketPulseClient") client: OkHttpClient,
        moshi: Moshi
    ): MarketPulseApi {
        return Retrofit.Builder()
            .baseUrl(MARKET_PULSE_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MarketPulseApi::class.java)
    }
}