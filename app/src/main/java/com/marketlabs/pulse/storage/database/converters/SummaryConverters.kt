package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketDriver
import com.marketlabs.pulse.storage.model.summary.MarketPosition
import com.marketlabs.pulse.storage.model.summary.MarketSentiment
import com.marketlabs.pulse.storage.model.summary.MarketVerdict
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.RiskItem
import com.marketlabs.pulse.storage.model.summary.WatchItem
import com.marketlabs.pulse.storage.model.summary.WhatsNewItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class SummaryConverters {
    private val moshi = Moshi.Builder().build()

    // 1. Verdict Converter
    @TypeConverter
    fun fromVerdict(verdict: MarketVerdict?): String? {
        if (verdict == null) return null
        return moshi.adapter(MarketVerdict::class.java).toJson(verdict)
    }

    @TypeConverter
    fun toVerdict(json: String?): MarketVerdict? {
        if (json.isNullOrBlank()) return null
        return moshi.adapter(MarketVerdict::class.java).fromJson(json)
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

    // 5. Drivers (List<MarketDriver>) Converter
    @TypeConverter
    fun fromDriverList(list: List<MarketDriver>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, MarketDriver::class.java)
        return moshi.adapter<List<MarketDriver>>(type).toJson(list)
    }

    @TypeConverter
    fun toDriverList(json: String?): List<MarketDriver>? {
        if (json.isNullOrBlank()) return null
        val type = Types.newParameterizedType(List::class.java, MarketDriver::class.java)
        return moshi.adapter<List<MarketDriver>>(type).fromJson(json)
    }

    // 6. Market Position Converter
    @TypeConverter
    fun fromPosition(position: MarketPosition?): String? {
        if (position == null) return null
        return moshi.adapter(MarketPosition::class.java).toJson(position)
    }

    @TypeConverter
    fun toPosition(json: String?): MarketPosition? {
        if (json.isNullOrBlank()) return null
        return moshi.adapter(MarketPosition::class.java).fromJson(json)
    }

    // 7. Watch (List<WatchItem>) Converter
    @TypeConverter
    fun fromWatchList(list: List<WatchItem>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, WatchItem::class.java)
        return moshi.adapter<List<WatchItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toWatchList(json: String?): List<WatchItem>? {
        if (json.isNullOrBlank()) return null
        val type = Types.newParameterizedType(List::class.java, WatchItem::class.java)
        return moshi.adapter<List<WatchItem>>(type).fromJson(json)
    }

    // 8. Risks (List<RiskItem>) Converter
    @TypeConverter
    fun fromRiskList(list: List<RiskItem>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, RiskItem::class.java)
        return moshi.adapter<List<RiskItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toRiskList(json: String?): List<RiskItem>? {
        if (json.isNullOrBlank()) return null
        val type = Types.newParameterizedType(List::class.java, RiskItem::class.java)
        return moshi.adapter<List<RiskItem>>(type).fromJson(json)
    }

    // 9. What's New (List<WhatsNewItem>) Converter -- new 2026-08-21
    @TypeConverter
    fun fromWhatsNewList(list: List<WhatsNewItem>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, WhatsNewItem::class.java)
        return moshi.adapter<List<WhatsNewItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toWhatsNewList(json: String?): List<WhatsNewItem>? {
        if (json.isNullOrBlank()) return null
        val type = Types.newParameterizedType(List::class.java, WhatsNewItem::class.java)
        return moshi.adapter<List<WhatsNewItem>>(type).fromJson(json)
    }

    // 10. Market Sentiment Converter -- new 2026-09-02
    @TypeConverter
    fun fromMarketSentiment(sentiment: MarketSentiment?): String? {
        if (sentiment == null) return null
        return moshi.adapter(MarketSentiment::class.java).toJson(sentiment)
    }

    @TypeConverter
    fun toMarketSentiment(json: String?): MarketSentiment? {
        if (json.isNullOrBlank()) return null
        return moshi.adapter(MarketSentiment::class.java).fromJson(json)
    }
}
