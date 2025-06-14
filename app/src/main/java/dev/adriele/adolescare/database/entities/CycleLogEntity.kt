package dev.adriele.adolescare.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import dev.adriele.adolescare.Utility

@Entity(
    tableName = "cycle_logs",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class CycleLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val cycleDay: Int,
    val date: String,
    val symptoms: List<String>?, // JSON string (e.g., ["cramps", "headache"])
    val sexActivity: List<String>?, // e.g., "protected", "unprotected"
    val pregnancyTestResult: List<String>?, // "positive", "negative", etc.
    val notes: String?,
    val createdAt: String = Utility.getCurrentDate()
)
