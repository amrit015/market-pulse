package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.indicators.DomainAiSynthesis
import com.marketlabs.pulse.storage.model.indicators.DomainIndicatorPillar
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class IndicatorsConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // 🧠 AI SYNTHESIS
    @TypeConverter
    fun fromAiSynthesis(data: DomainAiSynthesis?): String? =
        data?.let { moshi.adapter(DomainAiSynthesis::class.java).toJson(it) }

    @TypeConverter
    fun toAiSynthesis(json: String?): DomainAiSynthesis? =
        json?.let { moshi.adapter(DomainAiSynthesis::class.java).fromJson(it) }

    // 📊 UNIFIED QUANTITATIVE PILLAR (Tactical, Risk, Valuation, Vitals)
    @TypeConverter
    fun fromIndicatorPillar(data: DomainIndicatorPillar?): String? =
        data?.let { moshi.adapter(DomainIndicatorPillar::class.java).toJson(it) }

    @TypeConverter
    fun toIndicatorPillar(json: String?): DomainIndicatorPillar? =
        json?.let { moshi.adapter(DomainIndicatorPillar::class.java).fromJson(it) }
}