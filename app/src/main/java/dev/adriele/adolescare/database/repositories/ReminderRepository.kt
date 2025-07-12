package dev.adriele.adolescare.database.repositories

import dev.adriele.adolescare.database.entities.Reminder

interface ReminderRepository {

    suspend fun insertReminder(reminder: Reminder)

    suspend fun getReminderByTitleAndDate(userId: String, title: String, date: String): Reminder?

}