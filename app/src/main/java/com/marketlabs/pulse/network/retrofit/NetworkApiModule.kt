package com.marketlabs.pulse.network.retrofit

import com.marketlabs.pulse.network.api.ChartsApi
import com.marketlabs.pulse.network.api.DashboardApi
import com.marketlabs.pulse.network.api.IndicatorsApi
import com.marketlabs.pulse.network.api.IntradayApi
import com.marketlabs.pulse.network.api.MarketPositioningApi
import com.marketlabs.pulse.network.api.MarketPostureApi
import com.marketlabs.pulse.network.api.MarketPulseApi
import com.marketlabs.pulse.network.api.MarketRiskApi
import com.marketlabs.pulse.network.api.NewsApi
import com.marketlabs.pulse.network.api.StocksApi
import com.marketlabs.pulse.network.api.WeeklyPlaybookApi
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
    @Named("MarketPulseClient")
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
    @Named("MarketPulseRetrofit")
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
    fun provideMarketRiskApi(
        @Named("MarketPulseRetrofit") retrofit: Retrofit
    ): MarketRiskApi {
        return retrofit.create(MarketRiskApi::class.java)
    }

    @Provides
    @Singleton
    fun provideIndicatorsApi(
        @Named("MarketPulseRetrofit") retrofit: Retrofit
    ): IndicatorsApi {
        return retrofit.create(IndicatorsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDashboardApi(
        @Named("MarketPulseRetrofit") retrofit: Retrofit
    ): DashboardApi {
        return retrofit.create(DashboardApi::class.java)
    }


    @Provides
    @Singleton
    fun provideWeeklyPlaybookApi(
        @Named("MarketPulseRetrofit") retrofit: Retrofit
    ): WeeklyPlaybookApi {
        return retrofit.create(WeeklyPlaybookApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMarketPostureApi(
        @Named("MarketPulseRetrofit") retrofit: Retrofit
    ): MarketPostureApi {
        return retrofit.create(MarketPostureApi::class.java)
    }

    /** Provides the Retrofit client for `GET /insights/positioning` (retail sentiment / COT / short interest). */
    @Provides
    @Singleton
    fun provideMarketPositioningApi(
        @Named("MarketPulseRetrofit") retrofit: Retrofit
    ): MarketPositioningApi {
        return retrofit.create(MarketPositioningApi::class.java)
    }

    /** Provides the Retrofit client for the "Individual Stock Analysis" endpoints in marketPulse.ts. */
    @Provides
    @Singleton
    fun provideStocksApi(
        @Named("MarketPulseRetrofit") retrofit: Retrofit
    ): StocksApi {
        return retrofit.create(StocksApi::class.java)
    }

    /** Provides the Retrofit client for `GET /charts/:symbol` — period charts (5D/1M/6M/YTD/1Y). */
    @Provides
    @Singleton
    fun provideChartsApi(
        @Named("MarketPulseRetrofit") retrofit: Retrofit
    ): ChartsApi {
        return retrofit.create(ChartsApi::class.java)
    }

    /** Provides the Retrofit client for `GET /intraday/:symbol` — today's sparkline bars. */
    @Provides
    @Singleton
    fun provideIntradayApi(
        @Named("MarketPulseRetrofit") retrofit: Retrofit
    ): IntradayApi {
        return retrofit.create(IntradayApi::class.java)
    }
}