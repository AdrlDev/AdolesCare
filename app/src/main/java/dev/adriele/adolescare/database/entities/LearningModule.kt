package dev.adriele.adolescare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.adriele.adolescare.helpers.enums.ModuleContentType

@Entity(tableName = "modules")
data class LearningModule(
    @PrimaryKey val id: String,
    val title: String,
    val author: String = "",
    val category: String,
    val contentType: ModuleContentType, // ARTICLE or VIDEO
    val contentUrl: String,
    val contentCreditsUrl: String? = null,
    val orderBy: Int
)
