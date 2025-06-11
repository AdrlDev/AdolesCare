package dev.adriele.adolescare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycles")
data class MenstrualCycle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val lastPeriodStart: String,
    val periodDurationDays: Int,
    val cycleLengthWeeks: Int
)
