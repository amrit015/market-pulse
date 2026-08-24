package com.marketlabs.pulse.storage.database.converters

import android.util.Log
import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.indicators.DomainAiSynthesis
import com.marketlabs.pulse.storage.model.indicators.DomainIndicatorPillar
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class IndicatorsConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // 🧠 AI SYNTHESIS
    @TypeConverter
    fun fromAiSynthesis(data: DomainAiSynthesis?): String? =
        data?.let { moshi.adapter(DomainAiSynthesis::class.java).toJson(it) }

    // 💡 A device that cached a row before the schema_version 2 rollout has this column holding
    // the old shape (overarching_condition/pillar_glances/{briefing,key_driver,what_to_do} per
    // horizon) -- its field names don't exist on the new DomainAiSynthesis at all, so Moshi's
    // reflective adapter throws JsonDataException on the new shape's required non-null fields
    // rather than silently returning nulls. Treated as a one-time cache miss, same as "no
    // synthesis fetched yet": every aiSynthesis-consuming composable already null-checks this, so
    // returning null here forces a fresh Remote fetch through the exact same path.
    @TypeConverter
    fun toAiSynthesis(json: String?): DomainAiSynthesis? {
        if (json == null) return null
        return try {
            moshi.adapter(DomainAiSynthesis::class.java).fromJson(json)
        } catch (e: JsonDataException) {
            Log.w("IndicatorsConverters", "Cached ai_synthesis JSON doesn't match schema_version 2 shape -- treating as cache miss.", e)
            null
        }
    }

    // 📊 UNIFIED QUANTITATIVE PILLAR (Tactical, Risk, Valuation, Vitals)
    @TypeConverter
    fun fromIndicatorPillar(data: DomainIndicatorPillar?): String? =
        data?.let { moshi.adapter(DomainIndicatorPillar::class.java).toJson(it) }

    @TypeConverter
    fun toIndicatorPillar(json: String?): DomainIndicatorPillar? =
        json?.let { moshi.adapter(DomainIndicatorPillar::class.java).fromJson(it) }
}