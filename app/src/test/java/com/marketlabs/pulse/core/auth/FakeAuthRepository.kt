package com.marketlabs.pulse.core.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(
    initialUid: String? = null,
    var token: String? = null,
    /** How many `ensureSignedIn` calls fail before one succeeds. */
    var failuresBeforeSuccess: Int = 0,
    var tokenDelayMs: Long = 0L
) : AuthRepository {

    private val state = MutableStateFlow(initialUid)

    var signInAttempts = 0
        private set

    override val uid: Flow<String?> = state

    override fun currentUid(): String? = state.value

    override suspend fun ensureSignedIn(): Boolean {
        if (state.value != null) return true
        signInAttempts++
        if (failuresBeforeSuccess > 0) {
            failuresBeforeSuccess--
            return false
        }
        state.value = "fake-uid"
        return true
    }

    override suspend fun idToken(): String? {
        if (tokenDelayMs > 0) kotlinx.coroutines.delay(tokenDelayMs)
        return token
    }
}
