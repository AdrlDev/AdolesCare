package dev.adriele.adolescare.database.repositories

import dev.adriele.adolescare.database.entities.ArchiveRecentReadAndWatch
import dev.adriele.adolescare.database.entities.ArchiveReminder
import dev.adriele.adolescare.model.ArchiveItem

interface ArchiveRepository {
    suspend fun insertArchiveReminder(reminder: ArchiveReminder)
    suspend fun insertArchiveReadAndWatch(recentReadAndWatch: ArchiveRecentReadAndWatch)
    suspend fun getAllArchiveItems(): List<ArchiveItem>
    suspend fun getArchiveReminder(userId: String): List<ArchiveReminder>
    suspend fun getArchiveRecentReadNWatch(): List<ArchiveRecentReadAndWatch>
}