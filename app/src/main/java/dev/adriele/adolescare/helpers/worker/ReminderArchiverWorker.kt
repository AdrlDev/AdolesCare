package dev.adriele.adolescare.helpers.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.ArchiveRecentReadAndWatch
import dev.adriele.adolescare.database.entities.ArchiveReminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderArchiverWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val reminderDao = AppDatabaseProvider.getDatabase(context).reminderDao()
    private val recentReadAndWatchDao = AppDatabaseProvider.getDatabase(context).recentReadAndWatchDao()
    private val archiveDao = AppDatabaseProvider.getDatabase(context).archiveDao()

    override suspend fun doWork(): Result {
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val now = Date()
            val cutoffTime = now.time - 24 * 60 * 60 * 1000 // 24 hours ago

            // 🕒 Archive Reminders older than 24 hours
            val allReminders = reminderDao.getReminders() // <- you should have a suspend function for this
            allReminders.forEach { reminder ->
                try {
                    val reminderDate = sdf.parse(reminder.dateTime)
                    if (reminderDate != null && reminderDate.time <= cutoffTime) {
                        val archive = ArchiveReminder(
                            id = reminder.id,
                            userId = reminder.userId,
                            title = reminder.title,
                            message = reminder.message,
                            dateTime = reminder.dateTime,
                            type = reminder.type
                        )
                        archiveDao.insertArchiveReminder(archive)
                        reminderDao.deleteUserReminders(reminder.userId, reminder.id)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 📚 Archive RecentReadAndWatch older than 24 hours
            val readsToArchive = recentReadAndWatchDao.getAllRecentReadAndWatch()
            readsToArchive.forEach { read ->
                if (read.timestamp <= cutoffTime) {
                    val archiveReadNWatch = ArchiveRecentReadAndWatch(
                        id = read.id,
                        moduleId = read.moduleId,
                        timestamp = read.timestamp
                    )
                    archiveDao.insertArchiveReadAndWatch(archiveReadNWatch)
                    recentReadAndWatchDao.deleteRecentReadAndWatch(read.id)
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
