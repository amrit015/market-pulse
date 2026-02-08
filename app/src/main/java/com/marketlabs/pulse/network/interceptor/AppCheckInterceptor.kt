package com.marketlabs.pulse.network.interceptor

import com.google.firebase.appcheck.FirebaseAppCheck
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AppCheckInterceptor @Inject constructor(): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // Use runBlocking because we need the token synchronously for the request
        val token = runBlocking {
            try {
                // Fetch the App Check token (forceRefresh = false to use cache)
                val result = FirebaseAppCheck.getInstance().getAppCheckToken(false).await()
                result.token
            } catch (e: Exception) {
                null
            }
        }

        token?.let {
            requestBuilder.addHeader("X-Firebase-AppCheck", it)
        }

        return chain.proceed(requestBuilder.build())
    }
}