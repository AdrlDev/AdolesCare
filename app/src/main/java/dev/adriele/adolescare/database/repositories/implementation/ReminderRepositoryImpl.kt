package dev.adriele.adolescare.database.repositories.implementation

import dev.adriele.adolescare.database.dao.ReminderDao
import dev.adriele.adolescare.database.entities.Reminder
import dev.adriele.adolescare.database.repositories.ReminderRepository

class ReminderRepositoryImpl(
    private val dao: ReminderDao
): ReminderRepository {
    override suspend fun insertReminder(reminder: Reminder) {
        dao.insert(reminder = reminder)
    }

    override suspend fun getReminderByTitleAndDate(
        userId: String,
        title: String,
        date: String
    ): Reminder? {
        return dao.getReminderByTitleAndDate(
            userId = userId,
            title = title,
            date = date
        )
    }

    override suspend fun getAllReminders(userId: String): List<Reminder> {
        return dao.getUserReminders(userId)
    }
}