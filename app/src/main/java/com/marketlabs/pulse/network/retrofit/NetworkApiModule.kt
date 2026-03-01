package com.marketlabs.pulse.network.retrofit

import com.marketlabs.pulse.network.api.IndicatorsApi
import com.marketlabs.pulse.network.api.MarketPulseApi
import com.marketlabs.pulse.network.api.NewsApi
import com.marketlabs.pulse.network.api.RiskRadarApi
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
object NetworkApiModule {

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

    // 💡 NEW: A dedicated Retrofit provider
    @Provides
    @Singleton
    @Named("MarketPulseRetrofit") // Separates this from the FinnHub Retrofit
    fun provideMarketPulseRetrofit(
        @Named("MarketPulseClient") client: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(MARKET_PULSE_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * Different instances of api retrofit for different endpoints
     */
    @Provides
    @Singleton
    fun provideMarketPulseApi(
        @Named("MarketPulseRetrofit") retrofit: Retrofit
    ): MarketPulseApi {
        return retrofit.create(MarketPulseApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNewsApi(
        @Named("MarketPulseRetrofit") retrofit: Retrofit
    ): NewsApi {
        return retrofit.create(NewsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRiskRadarApi(
        @Named("MarketPulseRetrofit") retrofit: Retrofit
    ): RiskRadarApi {
        return retrofit.create(RiskRadarApi::class.java)
    }


    @Provides
    @Singleton
    fun provideIndicatorsApi(
        @Named("MarketPulseRetrofit") retrofit: Retrofit
    ): IndicatorsApi {
        return retrofit.create(IndicatorsApi::class.java)
    }
}