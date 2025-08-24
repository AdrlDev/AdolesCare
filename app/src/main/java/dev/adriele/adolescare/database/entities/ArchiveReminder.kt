package dev.adriele.adolescare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "archive_reminders")
data class ArchiveReminder(
    @PrimaryKey val id: Int = 0,
    val userId: String,
    val title: String,
    val message: String,
    val dateTime: String,
    val type: String
)
