package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.charts.ChartPoint
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/** (De)serializes `ChartEntity.points` to/from a JSON string column, same pattern as `StocksConverters`. */
class ChartsConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val pointListType = Types.newParameterizedType(List::class.java, ChartPoint::class.java)
    private val pointListAdapter = moshi.adapter<List<ChartPoint>>(pointListType)

    @TypeConverter
    fun fromPoints(data: List<ChartPoint>?): String? =
        data?.let { pointListAdapter.toJson(it) }

    @TypeConverter
    fun toPoints(json: String?): List<ChartPoint>? =
        json?.let { pointListAdapter.fromJson(it) }
}
