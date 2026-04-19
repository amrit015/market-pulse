package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyEvent
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class WeeklyPlaybookConverters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val eventListType = Types.newParameterizedType(List::class.java, WeeklyEvent::class.java)
    private val eventListAdapter = moshi.adapter<List<WeeklyEvent>>(eventListType)

    @TypeConverter
    fun fromWeeklyEvents(events: List<WeeklyEvent>?): String? {
        if (events == null) return null
        return eventListAdapter.toJson(events)
    }

    @TypeConverter
    fun toWeeklyEvents(json: String?): List<WeeklyEvent>? {
        if (json.isNullOrBlank()) return null
        return eventListAdapter.fromJson(json)
    }
}