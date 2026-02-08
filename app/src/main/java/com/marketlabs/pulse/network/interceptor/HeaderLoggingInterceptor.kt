package com.marketlabs.pulse.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log
import javax.inject.Inject

class HeaderLoggingInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        Log.d("PulseNetwork", "--- 🚀 OUTGOING REQUEST ---")
        Log.d("PulseNetwork", "URL: ${request.url}")
        Log.d("PulseNetwork", "Method: ${request.method}")

        // Log all headers specifically
        request.headers.forEach { (name, value) ->
            if (name.equals("X-Firebase-AppCheck", ignoreCase = true)) {
                Log.d("PulseNetwork", "✅ HEADER: $name = ${value.take(10)}...[TRUNCATED]")
            } else {
                Log.d("PulseNetwork", "HEADER: $name = $value")
            }
        }

        val response = chain.proceed(request)

        Log.d("PulseNetwork", "--- 📥 INCOMING RESPONSE ---")
        Log.d("PulseNetwork", "Code: ${response.code}")

        return response
    }
}