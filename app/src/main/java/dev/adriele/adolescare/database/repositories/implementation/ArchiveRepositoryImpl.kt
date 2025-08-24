package dev.adriele.adolescare.database.repositories.implementation

import dev.adriele.adolescare.database.dao.ArchiveDao
import dev.adriele.adolescare.database.dao.ReminderDao
import dev.adriele.adolescare.database.entities.ArchiveRecentReadAndWatch
import dev.adriele.adolescare.database.entities.ArchiveReminder
import dev.adriele.adolescare.database.entities.Reminder
import dev.adriele.adolescare.database.repositories.ArchiveRepository
import dev.adriele.adolescare.database.repositories.ReminderRepository
import dev.adriele.adolescare.model.ArchiveItem

class ArchiveRepositoryImpl(
    private val dao: ArchiveDao
): ArchiveRepository {
    override suspend fun insertArchiveReminder(reminder: ArchiveReminder) {
        dao.insertArchiveReminder(reminder)
    }

    override suspend fun insertArchiveReadAndWatch(recentReadAndWatch: ArchiveRecentReadAndWatch) {
        dao.insertArchiveReadAndWatch(recentReadAndWatch)
    }

    override suspend fun getAllArchiveItems(): List<ArchiveItem> {
        return dao.getAllArchiveItems()
    }

    override suspend fun getArchiveReminder(userId: String): List<ArchiveReminder> {
        return dao.getArchiveReminder(userId)
    }

    override suspend fun getArchiveRecentReadNWatch(): List<ArchiveRecentReadAndWatch> {
        return dao.getArchiveRecentReadNWatch()
    }
}