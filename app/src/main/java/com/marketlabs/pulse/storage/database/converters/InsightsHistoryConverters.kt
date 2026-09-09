package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.insights.InsightsHistoryPoint
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/** (De)serializes `InsightsHistoryEntity.points` to/from a JSON string column, same pattern as `MetricHistoryConverters`. */
class InsightsHistoryConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val pointListType = Types.newParameterizedType(List::class.java, InsightsHistoryPoint::class.java)
    private val pointListAdapter = moshi.adapter<List<InsightsHistoryPoint>>(pointListType)

    @TypeConverter
    fun fromPoints(data: List<InsightsHistoryPoint>?): String? =
        data?.let { pointListAdapter.toJson(it) }

    @TypeConverter
    fun toPoints(json: String?): List<InsightsHistoryPoint>? =
        json?.let { pointListAdapter.fromJson(it) }
}
