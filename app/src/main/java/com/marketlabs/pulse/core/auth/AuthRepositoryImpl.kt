package com.marketlabs.pulse.core.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val uid: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override fun currentUid(): String? = firebaseAuth.currentUser?.uid

    override suspend fun ensureSignedIn(): Boolean {
        if (firebaseAuth.currentUser != null) return true
        return try {
            firebaseAuth.signInAnonymously().await()
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Anonymous sign-in failed: ${e.message}")
            false
        }
    }

    override suspend fun idToken(): String? {
        val user = firebaseAuth.currentUser ?: return null
        return try {
            // `false` = use the cached token; never force a refresh.
            user.getIdToken(false).await().token
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "ID token unavailable: ${e.message}")
            null
        }
    }

    private companion object {
        const val TAG = "PulseAuth"
    }
}
