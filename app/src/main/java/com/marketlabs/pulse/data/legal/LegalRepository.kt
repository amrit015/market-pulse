package com.marketlabs.pulse.data.legal

import kotlinx.coroutines.flow.Flow

/**
 * Lighter than a full three-tier domain repository — same reasoning as `ThemeRepository`: no
 * Remote/Local split, no Room caching, just a thin wrapper over a local DataStore value. Still kept
 * as an `Impl -> Interface` pair per this repo's DI convention.
 */
interface LegalRepository {

    /** Emits 0 (never accepted) until the user has accepted a version; persisted after. */
    val acceptedVersion: Flow<Int>

    /** Records acceptance of `CURRENT_LEGAL_VERSION` at the current time. */
    suspend fun acceptCurrentVersion()

    companion object {
        /**
         * Bump this to force re-acceptance (e.g. a material Terms/Privacy change) — `MainActivity`
         * gates its start destination on `acceptedVersion < CURRENT_LEGAL_VERSION`.
         *
         * Must equal the `version` in `assets/legal_privacy.json` and `assets/legal_terms.json`;
         * `LegalDocumentJsonTest` fails the build if they drift. The constant stays in code (rather
         * than being read from the JSON) because the start destination is decided before any asset
         * is parsed and a `const` is usable from annotations and tests.
         */
        const val CURRENT_LEGAL_VERSION = 3
    }
}
