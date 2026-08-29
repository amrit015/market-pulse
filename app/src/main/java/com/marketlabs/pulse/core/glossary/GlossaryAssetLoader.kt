package com.marketlabs.pulse.core.glossary

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.IOException

/**
 * Shared "read a bundled JSON asset, parse with Moshi" loader -- the same read-json-from-assets
 * logic `MetricGlossaryProvider` already had, pulled out once a second, third, and fourth glossary
 * needed the identical boilerplate. Two shapes: nested (a JSON object of named categories, each
 * itself a `term -> definition` object -- `market_glossary.json`/`risk_glossary.json`) and flat (a
 * single `term -> definition` object -- `stock_analysis_glossary.json`/`dashboard_glossary.json`).
 */
internal fun loadNestedGlossaryJson(context: Context, fileName: String, tag: String): Map<String, Map<String, String>> {
    return try {
        val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val type = Types.newParameterizedType(
            Map::class.java, String::class.java,
            Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        )
        Moshi.Builder().build().adapter<Map<String, Map<String, String>>>(type).fromJson(json) ?: emptyMap()
    } catch (e: IOException) {
        Log.e(tag, "Failed to load $fileName", e)
        emptyMap()
    }
}

internal fun loadFlatGlossaryJson(context: Context, fileName: String, tag: String): Map<String, String> {
    return try {
        val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        Moshi.Builder().build().adapter<Map<String, String>>(type).fromJson(json) ?: emptyMap()
    } catch (e: IOException) {
        Log.e(tag, "Failed to load $fileName", e)
        emptyMap()
    }
}
