package dev.adriele.adolescare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class DailyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val date: String,
    val mood: String, // Comma-separated values
    val symptoms: String,
    val vaginalDischarge: String?,
    val digestionIssues: String,
    val physicalActivities: String,
    val sexualActivity: String,
    val pregnancyTestResult: String?
)
