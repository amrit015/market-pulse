package com.marketlabs.pulse.core.learn

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.IOException

/**
 * The 35 indicator + 5 event articles -- bundled as `assets/indicator_articles.json`, loaded once
 * and cached in memory for the process lifetime. The 35 indicator entries are reached one mechanism
 * at a time via that deck's own "See all indicators" (`TutorialsGaugesScreen`); the 5 event-only
 * entries (plus 6 of the 35, listed a second time as standard releases) live in the Learn hub's own
 * "Economic Events" section. Kept as its own file/provider rather than a new key inside
 * `learn_content.json`: at ~200 cards of prose this content is several times the size of everything
 * `learn_content.json` already holds, and the app already keeps a separate bundled-JSON concern for
 * glossary-shaped content (`metric_glossary.json`/[com.marketlabs.pulse.core.glossary.
 * MetricGlossaryProvider]) rather than folding it into Learn's own file -- this follows that same
 * precedent.
 *
 * Plain lazily-cached singleton `object`, not a Hilt type, for the same reason as
 * [LearnContentProvider]: `TutorialsHubScreen` and the generic per-key article screen are stateless
 * composables with no ViewModel in between. The ViewModels that also need a lookup
 * (`TutorialsGaugesViewModel`, `MetricDetailViewModel`, `GlossaryDetailViewModel`, the latter two to
 * decide whether to show a "Learn more" link) take an injected `Context` and call [get] directly,
 * same as any other consumer.
 */
object IndicatorArticlesProvider {
    private const val TAG = "IndicatorArticlesProvider"
    private var cache: Map<String, IndicatorArticle>? = null

    fun get(context: Context): Map<String, IndicatorArticle> {
        cache?.let { return it }
        return load(context.applicationContext).also { cache = it }
    }

    private fun load(context: Context): Map<String, IndicatorArticle> {
        return try {
            val json = context.assets.open("indicator_articles.json").bufferedReader().use { it.readText() }
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(Map::class.java, String::class.java, IndicatorArticle::class.java)
            moshi.adapter<Map<String, IndicatorArticle>>(type).fromJson(json) ?: emptyMap()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load indicator_articles.json", e)
            emptyMap()
        }
    }
}
