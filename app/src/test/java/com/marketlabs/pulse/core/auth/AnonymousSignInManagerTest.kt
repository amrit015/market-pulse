package com.marketlabs.pulse.core.auth

import com.marketlabs.pulse.data.legal.LegalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnonymousSignInManagerTest {

    private class FakeLegalRepository(initial: Int) : LegalRepository {
        val accepted = MutableStateFlow(initial)
        override val acceptedVersion: Flow<Int> = accepted
        override suspend fun acceptCurrentVersion() {
            accepted.value = LegalRepository.CURRENT_LEGAL_VERSION
        }
    }

    @Test
    fun doesNotSignInBeforeLegalAcceptance() = runTest {
        val auth = FakeAuthRepository()
        val legal = FakeLegalRepository(initial = 0)

        val job = AnonymousSignInManager(legal, auth).start(this)
        runCurrent()
        advanceTimeBy(AnonymousSignInManager.MAX_BACKOFF_MS * 2)

        assertEquals(0, auth.signInAttempts)
        assertNull(auth.currentUid())
        job.cancel()
    }

    @Test
    fun doesNotSignInForAnOlderAcceptedVersion() = runTest {
        val auth = FakeAuthRepository()
        val legal = FakeLegalRepository(initial = LegalRepository.CURRENT_LEGAL_VERSION - 1)

        val job = AnonymousSignInManager(legal, auth).start(this)
        runCurrent()

        assertEquals(0, auth.signInAttempts)
        job.cancel()
    }

    @Test
    fun signsInOnceTheCurrentVersionIsAccepted() = runTest {
        val auth = FakeAuthRepository()
        val legal = FakeLegalRepository(initial = 0)

        AnonymousSignInManager(legal, auth).start(this)
        runCurrent()
        assertNull(auth.currentUid())

        legal.acceptCurrentVersion()
        runCurrent()

        assertNotNull(auth.currentUid())
        assertEquals(1, auth.signInAttempts)
    }

    @Test
    fun retriesWithBackoffAfterAFailedSignIn() = runTest {
        val auth = FakeAuthRepository(failuresBeforeSuccess = 2)
        val legal = FakeLegalRepository(initial = LegalRepository.CURRENT_LEGAL_VERSION)

        AnonymousSignInManager(legal, auth).start(this)
        runCurrent()
        assertEquals(1, auth.signInAttempts)

        advanceTimeBy(AnonymousSignInManager.INITIAL_BACKOFF_MS + 1)
        runCurrent()
        assertEquals(2, auth.signInAttempts)
        assertNull(auth.currentUid())

        advanceTimeBy(AnonymousSignInManager.INITIAL_BACKOFF_MS * 2 + 1)
        runCurrent()
        assertEquals(3, auth.signInAttempts)
        assertNotNull(auth.currentUid())
    }
}
