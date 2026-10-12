package com.marketlabs.pulse.core.auth

import kotlinx.coroutines.flow.Flow

/**
 * The app's anonymous Firebase identity. v1 has no sign-in UI: the user is created silently, only
 * after the current legal version has been accepted (see [AnonymousSignInManager]), and exists so
 * the backend can recognise an install without knowing who is behind it.
 *
 * Nothing here may ever make a network request fail -- every accessor returns null / false rather
 * than throwing, because the callers (the OkHttp interceptor, the About row) treat "no identity
 * yet" as a normal state.
 */
interface AuthRepository {

    /** The current anonymous UID, or null before sign-in completes. Emits on every auth change. */
    val uid: Flow<String?>

    /** Synchronous snapshot of the UID the SDK has persisted, without touching the network. */
    fun currentUid(): String?

    /** Signs in anonymously if there is no user yet. Returns whether a user exists afterwards. */
    suspend fun ensureSignedIn(): Boolean

    /**
     * The SDK's cached ID token, or null with no user or on any failure. Never forces a refresh:
     * the SDK only goes to the network by itself when the cached token has expired.
     */
    suspend fun idToken(): String?
}
