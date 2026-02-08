package com.marketlabs.pulse.network.interceptor

import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AppCheckInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        Log.d("PulseAppCheck", "Requesting token from Firebase...")

        val token = try {
            // Use a short timeout so the app doesn't hang forever
            runBlocking {
                withTimeout(5000) {
                    val result = FirebaseAppCheck.getInstance()
                        .getAppCheckToken(false).await()
                    result.token
                }
            }
        } catch (e: Exception) {
            Log.e("PulseAppCheck", "💥 Token fetch failed: ${e.message}", e)
            null
        }

        if (token != null) {
            Log.d("PulseAppCheck", "✅ Token obtained: ${token.take(10)}...")
            requestBuilder.addHeader("X-Firebase-AppCheck", token)
        } else {
            Log.e("PulseAppCheck", "❌ No token found. Request will likely fail with 401.")
        }

        return chain.proceed(requestBuilder.build())
    }
}