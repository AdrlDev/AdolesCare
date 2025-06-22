package dev.adriele.adolescare.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import dev.adriele.adolescare.helpers.Utility

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
    val symptoms: List<String>? = null, // JSON string (e.g., ["cramps", "headache"])
    val sexActivity: List<String>? = null, // e.g., "protected", "unprotected"
    val pregnancyTestResult: List<String>? = null, // "positive", "negative", etc.
    val mood: List<String>? = null,
    val vaginalDischarge: List<String>? = null,
    val digestionAndStool: List<String>? = null,
    val physicalActivity: List<String>? = null,
    val notes: String? = null,
    val createdAt: String = Utility.getCurrentDate()
)
