package com.marketlabs.pulse.data.legal

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import java.io.IOException

/**
 * Loads the bundled legal documents once and caches them for the process lifetime. A plain
 * lazily-cached `object` rather than a Hilt-injected class for the same reason as
 * `LearnContentProvider`: the only consumers are stateless composables with no ViewModel in
 * between.
 */
object LegalDocumentProvider {
    private const val TAG = "LegalDocumentProvider"
    const val PRIVACY_ASSET = "legal_privacy.json"
    const val TERMS_ASSET = "legal_terms.json"

    private val cache = mutableMapOf<String, LegalDocument>()

    fun privacy(context: Context): LegalDocument = load(context, PRIVACY_ASSET)

    fun terms(context: Context): LegalDocument = load(context, TERMS_ASSET)

    @Synchronized
    private fun load(context: Context, assetName: String): LegalDocument {
        cache[assetName]?.let { return it }
        val document = try {
            val json = context.applicationContext.assets.open(assetName).bufferedReader().use { it.readText() }
            Moshi.Builder().build().adapter(LegalDocument::class.java).fromJson(json)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load $assetName", e)
            null
        } ?: return EMPTY
        cache[assetName] = document
        return document
    }

    // Not cached, so a transient read failure doesn't pin an empty document for the whole process.
    private val EMPTY = LegalDocument(version = 0, lastUpdated = "", title = "", sections = emptyList())
}
