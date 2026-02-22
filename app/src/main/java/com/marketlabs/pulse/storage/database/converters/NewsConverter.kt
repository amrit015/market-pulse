package com.marketlabs.pulse.storage.database.converters

import androidx.room.TypeConverter
import com.marketlabs.pulse.storage.model.news.NewsArticle
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class NewsConverters {

    // 💡Add the Kotlin factory so Moshi can parse domain models
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // 1. News Articles Converter
    @TypeConverter
    fun fromNewsList(list: List<NewsArticle>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, NewsArticle::class.java)
        return moshi.adapter<List<NewsArticle>>(type).toJson(list)
    }

    @TypeConverter
    fun toNewsList(json: String?): List<NewsArticle>? {
        if (json.isNullOrBlank()) return null
        val type = Types.newParameterizedType(List::class.java, NewsArticle::class.java)
        return moshi.adapter<List<NewsArticle>>(type).fromJson(json)
    }

    // 2. Tags List (List<String>) Converter
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String>? {
        if (json.isNullOrBlank()) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).fromJson(json)
    }
}