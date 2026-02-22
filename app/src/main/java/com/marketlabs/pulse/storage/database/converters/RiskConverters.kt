package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.riskRadar.RiskGauges
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class RiskConverters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @TypeConverter
    fun fromRiskGauges(gauges: RiskGauges?): String? {
        if (gauges == null) return null
        return moshi.adapter(RiskGauges::class.java).toJson(gauges)
    }

    @TypeConverter
    fun toRiskGauges(json: String?): RiskGauges? {
        if (json.isNullOrBlank()) return null
        return moshi.adapter(RiskGauges::class.java).fromJson(json)
    }
}