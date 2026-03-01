package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.indicators.PhaseDetails
import com.marketlabs.pulse.storage.model.indicators.PhaseSummary
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class IndicatorsConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromPhaseSummary(summary: PhaseSummary): String =
        moshi.adapter(PhaseSummary::class.java).toJson(summary)

    @TypeConverter
    fun toPhaseSummary(json: String): PhaseSummary? =
        moshi.adapter(PhaseSummary::class.java).fromJson(json)

    @TypeConverter
    fun fromPhaseDetails(details: PhaseDetails): String =
        moshi.adapter(PhaseDetails::class.java).toJson(details)

    @TypeConverter
    fun toPhaseDetails(json: String): PhaseDetails? =
        moshi.adapter(PhaseDetails::class.java).fromJson(json)
}