package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskFactor
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class RiskConverters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val riskFactorListType = Types.newParameterizedType(List::class.java, MarketRiskFactor::class.java)
    private val riskFactorListAdapter = moshi.adapter<List<MarketRiskFactor>>(riskFactorListType)

    @TypeConverter
    fun fromMarketRiskFactors(risks: List<MarketRiskFactor>?): String? {
        if (risks == null) return null
        return riskFactorListAdapter.toJson(risks)
    }

    @TypeConverter
    fun toMarketRiskFactors(json: String?): List<MarketRiskFactor>? {
        if (json.isNullOrBlank()) return null
        return riskFactorListAdapter.fromJson(json)
    }
}