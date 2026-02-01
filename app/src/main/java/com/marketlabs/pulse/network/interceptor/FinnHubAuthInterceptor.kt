package com.marketlabs.pulse.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// This class automatically adds ?token=XYZ to every single request
class FinnHubAuthInterceptor @Inject constructor(
    private val apiKey: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalHttpUrl = originalRequest.url

        // 1. Modify the URL to add the query param
        val newUrl = originalHttpUrl.newBuilder()
            .addQueryParameter("token", apiKey)
            .build()

        // 2. Rebuild the request with the new URL
        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}