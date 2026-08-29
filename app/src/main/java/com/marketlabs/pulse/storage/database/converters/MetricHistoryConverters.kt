package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.indicators.MetricHistoryPoint
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/** (De)serializes `MetricHistoryEntity.points` to/from a JSON string column, same pattern as `ChartsConverters`. */
class MetricHistoryConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val pointListType = Types.newParameterizedType(List::class.java, MetricHistoryPoint::class.java)
    private val pointListAdapter = moshi.adapter<List<MetricHistoryPoint>>(pointListType)

    @TypeConverter
    fun fromPoints(data: List<MetricHistoryPoint>?): String? =
        data?.let { pointListAdapter.toJson(it) }

    @TypeConverter
    fun toPoints(json: String?): List<MetricHistoryPoint>? =
        json?.let { pointListAdapter.fromJson(it) }
}
