package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.pastReleases.PastRelease
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class PastReleasesConverters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val releaseMapType = Types.newParameterizedType(Map::class.java, String::class.java, PastRelease::class.java)
    private val releaseMapAdapter = moshi.adapter<Map<String, PastRelease>>(releaseMapType)

    @TypeConverter
    fun fromPastReleases(releases: Map<String, PastRelease>?): String? {
        if (releases == null) return null
        return releaseMapAdapter.toJson(releases)
    }

    @TypeConverter
    fun toPastReleases(json: String?): Map<String, PastRelease>? {
        if (json.isNullOrBlank()) return null
        return releaseMapAdapter.fromJson(json)
    }
}
