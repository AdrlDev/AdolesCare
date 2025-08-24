package dev.adriele.adolescare.model

import dev.adriele.adolescare.database.entities.ArchiveRecentReadAndWatch
import dev.adriele.adolescare.database.entities.ArchiveReminder

sealed class InsertState {
    object Idle : InsertState()
    object Loading : InsertState()
    object Success : InsertState()
    data class DataToArchive(val reminder: ArchiveReminder?, val recentReadNWatch: ArchiveRecentReadAndWatch?) : InsertState()
    data class Error(val message: String) : InsertState()
}