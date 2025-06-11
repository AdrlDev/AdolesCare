package dev.adriele.adolescare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "modules")
data class LearningModule(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val contentType: String, // ARTICLE or VIDEO
    val contentUrl: String
)
