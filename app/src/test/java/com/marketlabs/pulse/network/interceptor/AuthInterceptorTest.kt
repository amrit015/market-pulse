package com.marketlabs.pulse.network.interceptor

import com.marketlabs.pulse.core.auth.FakeAuthRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthInterceptorTest {

    /** Terminal interceptor: records what actually would have gone on the wire, returns 200. */
    private class Capture : Interceptor {
        var seen: Request? = null
        override fun intercept(chain: Interceptor.Chain): Response {
            seen = chain.request()
            return Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody())
                .build()
        }
    }

    private fun execute(auth: FakeAuthRepository): Pair<Response, Request?> {
        val capture = Capture()
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(auth))
            .addInterceptor(capture)
            .build()
        val response = client.newCall(Request.Builder().url("https://example.test/pulse/v3/latest").build()).execute()
        return response to capture.seen
    }

    @Test
    fun addsBearerHeaderWhenATokenExists() {
        val (response, sent) = execute(FakeAuthRepository(initialUid = "uid", token = "tok123"))

        assertEquals(200, response.code)
        assertEquals("Bearer tok123", sent?.header("Authorization"))
    }

    @Test
    fun omitsHeaderWhenThereIsNoUser() {
        val (response, sent) = execute(FakeAuthRepository(initialUid = null, token = "ignored"))

        assertEquals(200, response.code)
        assertNull(sent?.header("Authorization"))
    }

    @Test
    fun omitsHeaderWhenTheUserHasNoToken() {
        val (response, sent) = execute(FakeAuthRepository(initialUid = "uid", token = null))

        assertEquals(200, response.code)
        assertNull(sent?.header("Authorization"))
    }

    @Test
    fun omitsHeaderButStillSucceedsWhenTheTokenFetchTimesOut() {
        val slow = FakeAuthRepository(
            initialUid = "uid",
            token = "tok123",
            tokenDelayMs = AuthInterceptor.TOKEN_TIMEOUT_MS * 20
        )

        val (response, sent) = execute(slow)

        assertEquals(200, response.code)
        assertNull(sent?.header("Authorization"))
    }

    @Test
    fun keepsAnAuthorizationHeaderTheCallerAlreadySet() {
        val capture = Capture()
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(FakeAuthRepository(initialUid = "uid", token = "tok123")))
            .addInterceptor(capture)
            .build()
        client.newCall(
            Request.Builder().url("https://example.test/x").header("Authorization", "Bearer mine").build()
        ).execute()

        assertEquals("Bearer mine", capture.seen?.header("Authorization"))
    }
}
