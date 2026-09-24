package com.marketlabs.pulse.core.learn

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import java.io.IOException

/**
 * All Tutorials/"Learn" hub article and deck copy -- bundled as `assets/learn_content.json`, loaded
 * once and cached in memory for the process lifetime. Same reasoning as `MarketGlossaryProvider`
 * for being a plain lazily-cached singleton `object` rather than a Hilt `@Singleton @Inject` class:
 * every call site (`TutorialsConceptScreen`, `MechanismDeckScreen`, `TutorialsGaugeAnatomyScreen`,
 * `TutorialsDataLimitationsScreen`, `TutorialsHubScreen`) is a stateless composable with no
 * ViewModel in between -- `get(LocalContext.current)` is a one-line call from any of them.
 *
 * Card heading labels ("What this is", "Key ideas", ...) and the hub's section/sub-group headers
 * stay as string resources -- they're fixed, reused UI labels, not per-article content, and this
 * file would otherwise repeat the same handful of strings dozens of times.
 */
object LearnContentProvider {
    private const val TAG = "LearnContentProvider"
    private var cache: LearnContent? = null

    fun get(context: Context): LearnContent {
        cache?.let { return it }
        return load(context.applicationContext).also { cache = it }
    }

    private fun load(context: Context): LearnContent {
        return try {
            val json = context.assets.open("learn_content.json").bufferedReader().use { it.readText() }
            val adapter = Moshi.Builder().build().adapter(LearnContentJson::class.java)
            adapter.fromJson(json)?.resolve() ?: emptyLearnContent()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load learn_content.json", e)
            emptyLearnContent()
        }
    }

    private fun emptyLearnContent() = LearnContent(
        conceptArticles = emptyMap(),
        mechanismDecks = emptyMap(),
        gaugeAnatomy = LearnStandaloneArticle(title = "", cards = emptyList()),
        dataLimitations = LearnStandaloneArticle(title = "", cards = emptyList())
    )
}
