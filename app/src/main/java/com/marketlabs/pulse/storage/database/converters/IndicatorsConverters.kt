package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.indicators.DomainMacroVitals
import com.marketlabs.pulse.storage.model.indicators.DomainMarketAction
import com.marketlabs.pulse.storage.model.indicators.DomainMarketPhase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class IndicatorsConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // 🚦 MARKET PHASE
    @TypeConverter
    fun fromMarketPhase(data: DomainMarketPhase?): String? =
        data?.let { moshi.adapter(DomainMarketPhase::class.java).toJson(it) }

    @TypeConverter
    fun toMarketPhase(json: String?): DomainMarketPhase? =
        json?.let { moshi.adapter(DomainMarketPhase::class.java).fromJson(it) }

    // 🏥 MACRO VITALS
    @TypeConverter
    fun fromMacroVitals(data: DomainMacroVitals?): String? =
        data?.let { moshi.adapter(DomainMacroVitals::class.java).toJson(it) }

    @TypeConverter
    fun toMacroVitals(json: String?): DomainMacroVitals? =
        json?.let { moshi.adapter(DomainMacroVitals::class.java).fromJson(it) }

    // 🎯 MARKET ACTION
    @TypeConverter
    fun fromMarketAction(data: DomainMarketAction?): String? =
        data?.let { moshi.adapter(DomainMarketAction::class.java).toJson(it) }

    @TypeConverter
    fun toMarketAction(json: String?): DomainMarketAction? =
        json?.let { moshi.adapter(DomainMarketAction::class.java).fromJson(it) }
}