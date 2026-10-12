package com.marketlabs.pulse.network.interceptor

import com.marketlabs.pulse.core.auth.AuthRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Adds the anonymous user's ID token as `Authorization: Bearer <token>`.
 *
 * Auth must never cost a request: with no user, no token, any failure, or a token fetch slower
 * than [TOKEN_TIMEOUT_MS], the request goes out unchanged. The backend does not require the header
 * in v1.
 */
class AuthInterceptor @Inject constructor(
    private val authRepository: AuthRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header(AUTHORIZATION) != null) return chain.proceed(request)

        // Skip the blocking call entirely before sign-in: the common case on a first launch.
        val token = if (authRepository.currentUid() == null) {
            null
        } else {
            try {
                runBlocking { withTimeoutOrNull(TOKEN_TIMEOUT_MS) { authRepository.idToken() } }
            } catch (e: Exception) {
                null
            }
        }

        if (token.isNullOrEmpty()) return chain.proceed(request)
        return chain.proceed(request.newBuilder().header(AUTHORIZATION, "Bearer $token").build())
    }

    internal companion object {
        const val AUTHORIZATION = "Authorization"
        const val TOKEN_TIMEOUT_MS = 1_500L
    }
}
