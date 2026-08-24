package com.marketlabs.pulse.core.glossary

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Static per-metric glossary content (what it is / how to read / bands / gotchas) for all 26
 * tracked indicators, bundled as `assets/metric_glossary.json` and keyed by `metric_id` -- the
 * same ids the Indicators domain's live metrics and the AI synthesis's `shifts[]` already use.
 * Loaded once and cached in memory for the process lifetime; 26 short entries never justify Room
 * or per-metric lazy loading.
 *
 * Band `label`s in the bundle are written to match the live `signal_text` values
 * classifierLogic.ts's `getMetricUiState()` actually emits for that metric -- the detail sheet
 * highlights whichever band's label equals the metric's current `signal_text`. If a backend
 * classifier change ever moves a label, the current-band highlight just stops matching (falls
 * back to an unhighlighted list, see the sheet) rather than crashing -- but the bundle needs a
 * matching update to stay accurate.
 */
@Singleton
class MetricGlossaryProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val glossary: Map<String, MetricGlossaryEntry> by lazy { loadGlossary() }

    fun get(metricId: String): MetricGlossaryEntry? = glossary[metricId]

    private fun loadGlossary(): Map<String, MetricGlossaryEntry> {
        return try {
            val json = context.assets.open("metric_glossary.json").bufferedReader().use { it.readText() }
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(Map::class.java, String::class.java, MetricGlossaryEntry::class.java)
            moshi.adapter<Map<String, MetricGlossaryEntry>>(type).fromJson(json) ?: emptyMap()
        } catch (e: IOException) {
            Log.e("MetricGlossaryProvider", "Failed to load metric_glossary.json", e)
            emptyMap()
        }
    }
}
