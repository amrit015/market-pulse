package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketOutlook
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Verdict
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class SummaryConverters {
    private val moshi = Moshi.Builder().build()

    // 1. Verdict Converter
    @TypeConverter
    fun fromVerdict(verdict: Verdict?): String? {
        if (verdict == null) return null
        return moshi.adapter(Verdict::class.java).toJson(verdict)
    }

    @TypeConverter
    fun toVerdict(json: String?): Verdict? {
        if (json.isNullOrBlank()) return null
        return moshi.adapter(Verdict::class.java).fromJson(json)
    }

    // 2. Lead Stories (List<NewsItem>) Converter
    @TypeConverter
    fun fromNewsList(list: List<NewsItem>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, NewsItem::class.java)
        return moshi.adapter<List<NewsItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toNewsList(json: String?): List<NewsItem>? {
        if (json.isNullOrBlank()) return null
        val type = Types.newParameterizedType(List::class.java, NewsItem::class.java)
        return moshi.adapter<List<NewsItem>>(type).fromJson(json)
    }

    // 3. Macro Mix (List<MacroItem>) Converter
    @TypeConverter
    fun fromMacroList(list: List<MacroItem>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, MacroItem::class.java)
        return moshi.adapter<List<MacroItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toMacroList(json: String?): List<MacroItem>? {
        if (json.isNullOrBlank()) return null
        val type = Types.newParameterizedType(List::class.java, MacroItem::class.java)
        return moshi.adapter<List<MacroItem>>(type).fromJson(json)
    }

    // 4. Domino Effect Converter
    @TypeConverter
    fun fromDomino(domino: DominoEffect?): String? {
        if (domino == null) return null
        return moshi.adapter(DominoEffect::class.java).toJson(domino)
    }

    @TypeConverter
    fun toDomino(json: String?): DominoEffect? {
        if (json.isNullOrBlank()) return null
        return moshi.adapter(DominoEffect::class.java).fromJson(json)
    }

    // 5. Market Lookout Converter
    @TypeConverter
    fun fromLookout(lookout: MarketOutlook?): String? {
        if (lookout == null) return null
        return moshi.adapter(MarketOutlook::class.java).toJson(lookout)
    }

    @TypeConverter
    fun toLookout(json: String?): MarketOutlook? {
        if (json.isNullOrBlank()) return null
        return moshi.adapter(MarketOutlook::class.java).fromJson(json)
    }
}