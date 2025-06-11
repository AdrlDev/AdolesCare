package dev.adriele.adolescare.chatbot

import androidx.room.TypeConverter

class ResponseTypeConverter {
    @TypeConverter
    fun toResponseType(value: String?): ResponseType? = value?.let { ResponseType.valueOf(it) }

    @TypeConverter
    fun fromResponseType(responseType: ResponseType?): String? = responseType?.name
}