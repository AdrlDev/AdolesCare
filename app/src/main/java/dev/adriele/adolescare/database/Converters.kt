package dev.adriele.adolescare.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.adriele.adolescare.api.response.Sources
import dev.adriele.adolescare.helpers.enums.ModuleContentType

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromModuleContentType(value: ModuleContentType): String {
        return value.name
    }

    @TypeConverter
    fun toModuleContentType(value: String): ModuleContentType {
        return ModuleContentType.valueOf(value)
    }

    @TypeConverter
    fun fromSourcesList(value: List<Sources>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toSourcesList(value: String): List<Sources>? {
        val listType = object : TypeToken<List<Sources>>() {}.type
        return Gson().fromJson(value, listType)
    }
}