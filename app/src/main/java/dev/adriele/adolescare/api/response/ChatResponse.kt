package dev.adriele.adolescare.api.response

import androidx.annotation.Nullable
import com.google.gson.annotations.Expose
import kotlinx.serialization.SerialName

data class OuterResponse(
    val answer: ChatResponse,
    val sources: List<Sources>
)

data class ChatResponse(
    val query: String,
    val result: String
)

data class Sources(
    @Expose
    @SerialName("producer")
    val producer: String,
    @Expose
    @SerialName("creator")
    val creator: String,
    @Expose
    @SerialName("creationdate")
    val creationDate: String,
    @Expose
    @SerialName("author")
    val author: String,
    @Expose
    @SerialName("moddate")
    val modDate: String,
    @Expose
    @SerialName("title")
    val title: String?,
    @Expose
    @SerialName("source")
    val source: String,
    @Expose
    @SerialName("total_pages")
    val totalPages: String,
    @Expose
    @SerialName("page")
    val page: String,
    @Expose
    @SerialName("page_label")
    val pageLabel: String
)
