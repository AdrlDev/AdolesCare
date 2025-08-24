package dev.adriele.adolescare.database.repositories

import dev.adriele.adolescare.database.entities.Reminder

interface ReminderRepository {

    suspend fun insertReminder(reminder: Reminder)
    suspend fun deleteReminder(userId: String, id: Int)

    suspend fun getReminderByTitleAndDate(userId: String, title: String, date: String): Reminder?
    suspend fun getReminderByMessageAndDate(userId: String, message: String, date: String): Reminder?

    suspend fun getAllReminders(userId: String): List<Reminder>

}