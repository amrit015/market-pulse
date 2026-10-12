package com.marketlabs.pulse.core.auth

import com.marketlabs.pulse.data.legal.LegalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creates the anonymous user once -- and only once -- the current legal version has been accepted.
 * Signing in earlier would create an identifier before the user has seen the privacy policy that
 * discloses it.
 *
 * Runs entirely in the scope it is started with: a failed sign-in (offline, for example) is retried
 * with exponential backoff in the background and never blocks or slows any UI. A process restart
 * starts the whole sequence again, and the SDK itself persists a successful user across launches.
 */
@Singleton
class AnonymousSignInManager @Inject constructor(
    private val legalRepository: LegalRepository,
    private val authRepository: AuthRepository
) {

    fun start(scope: CoroutineScope): Job = scope.launch {
        legalRepository.acceptedVersion.first { it >= LegalRepository.CURRENT_LEGAL_VERSION }

        var backoffMs = INITIAL_BACKOFF_MS
        while (!authRepository.ensureSignedIn()) {
            delay(backoffMs)
            backoffMs = (backoffMs * 2).coerceAtMost(MAX_BACKOFF_MS)
        }
    }

    internal companion object {
        const val INITIAL_BACKOFF_MS = 15_000L
        const val MAX_BACKOFF_MS = 10 * 60_000L
    }
}
