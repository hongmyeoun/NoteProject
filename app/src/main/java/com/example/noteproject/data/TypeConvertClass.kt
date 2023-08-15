package com.example.noteproject.data

import androidx.room.TypeConverter

class TypeConvertClass {
    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toList(data: String?): List<String>? {
        return data?.split(",")?.map { it.trim() }
    }
}