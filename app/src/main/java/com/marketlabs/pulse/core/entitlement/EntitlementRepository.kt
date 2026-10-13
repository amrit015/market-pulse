package com.marketlabs.pulse.core.entitlement

import kotlinx.coroutines.flow.StateFlow

/**
 * The one place the app asks "is this user Pro?". Every interaction a paid tier will lock reads
 * [isPro] rather than deciding for itself, so turning a gate on later is a change here, not a hunt
 * through the UI.
 *
 * In v1 everyone is entitled and nothing is locked.
 */
interface EntitlementRepository {
    val isPro: StateFlow<Boolean>
}
