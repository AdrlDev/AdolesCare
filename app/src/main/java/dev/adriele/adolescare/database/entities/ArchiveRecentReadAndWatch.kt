package dev.adriele.adolescare.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "archive_recent_read_and_watch",
    foreignKeys = [
        ForeignKey(
            entity = LearningModule::class,
            parentColumns = ["id"],
            childColumns = ["moduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ArchiveRecentReadAndWatch(
    @PrimaryKey val id: Long = 0,
    val moduleId: String, // Foreign key to LearningModule.id
    val timestamp: Long // When it was read or watched
)
