package com.example.noteproject.data

import androidx.room.TypeConverter

class TypeConvertClass {
    @TypeConverter
    fun fromStringList(value: String?): List<String?> {
        return value?.split(",") ?: emptyList()
    }

    @TypeConverter
    fun toStringList(list: List<String?>): String {
        return list.joinToString(",")
    }
    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toList(data: String?): List<String>? {
        return data?.split(",")?.map { it.trim() }
    }
}