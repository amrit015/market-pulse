package com.marketlabs.pulse.storage.database.converters

import android.util.Log
import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.positioning.DomainInstitutionalPositioning
import com.marketlabs.pulse.storage.model.positioning.DomainRetailSentiment
import com.marketlabs.pulse.storage.model.positioning.DomainShortInterest
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * `MarketPositioningEntity`'s three nested sections are each one JSON-blob TEXT column, same
 * approach as `IndicatorsConverters` for the indicators domain. `List<String>?` (synthesisContentFlags)
 * deliberately has no converter here -- see StocksConverters.kt's doc comment; NewsConverters
 * already registers that exact conversion database-wide.
 *
 * Greenfield domain (no pre-existing cached row shape to migrate off of), so no JsonDataException
 * degrade-to-null catch is needed yet the way IndicatorsConverters has for its schema_version 2
 * migration -- add one here if/when this domain's own backend shape ever changes underneath an
 * already-cached row.
 */
class PositioningConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromRetailSentiment(data: DomainRetailSentiment?): String? =
        data?.let { moshi.adapter(DomainRetailSentiment::class.java).toJson(it) }

    @TypeConverter
    fun toRetailSentiment(json: String?): DomainRetailSentiment? {
        if (json == null) return null
        return try {
            moshi.adapter(DomainRetailSentiment::class.java).fromJson(json)
        } catch (e: JsonDataException) {
            Log.w("PositioningConverters", "Cached retail_sentiment JSON doesn't match the current shape -- treating as cache miss.", e)
            null
        }
    }

    @TypeConverter
    fun fromInstitutionalPositioning(data: DomainInstitutionalPositioning?): String? =
        data?.let { moshi.adapter(DomainInstitutionalPositioning::class.java).toJson(it) }

    @TypeConverter
    fun toInstitutionalPositioning(json: String?): DomainInstitutionalPositioning? {
        if (json == null) return null
        return try {
            moshi.adapter(DomainInstitutionalPositioning::class.java).fromJson(json)
        } catch (e: JsonDataException) {
            Log.w("PositioningConverters", "Cached institutional_positioning JSON doesn't match the current shape -- treating as cache miss.", e)
            null
        }
    }

    @TypeConverter
    fun fromShortInterest(data: DomainShortInterest?): String? =
        data?.let { moshi.adapter(DomainShortInterest::class.java).toJson(it) }

    @TypeConverter
    fun toShortInterest(json: String?): DomainShortInterest? {
        if (json == null) return null
        return try {
            moshi.adapter(DomainShortInterest::class.java).fromJson(json)
        } catch (e: JsonDataException) {
            Log.w("PositioningConverters", "Cached short_interest JSON doesn't match the current shape -- treating as cache miss.", e)
            null
        }
    }
}
