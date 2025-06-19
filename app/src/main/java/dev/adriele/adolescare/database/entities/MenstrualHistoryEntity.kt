package dev.adriele.adolescare.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import dev.adriele.adolescare.helpers.Utility

@Entity(
    tableName = "menstrual_history",
    foreignKeys = [ForeignKey(
        entity = User::class, // Assuming you already have this
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MenstrualHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val firstPeriodReported: Boolean,
    val lastPeriodStart: String?,
    val periodDurationDays: Int?,
    val cycleIntervalWeeks: Int?,
    val dataSource: String = "user_input",
    val createdAt: String = Utility.getCurrentDate(),
    val updatedAt: String = Utility.getCurrentDate()
)