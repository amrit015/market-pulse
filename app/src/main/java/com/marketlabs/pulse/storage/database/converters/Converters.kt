package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.MarketTrend

class Converters {
    @TypeConverter
    fun fromTrend(trend: MarketTrend): String = trend.name

    @TypeConverter
    fun toTrend(value: String): MarketTrend = try {
        MarketTrend.valueOf(value)
    } catch (e: Exception) {
        MarketTrend.UNKNOWN
    }
}