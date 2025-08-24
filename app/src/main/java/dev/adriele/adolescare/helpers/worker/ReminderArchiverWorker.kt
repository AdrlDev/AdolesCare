package dev.adriele.adolescare.helpers.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.ArchiveRecentReadAndWatch
import dev.adriele.adolescare.database.entities.ArchiveReminder
import java.text.SimpleDateFormat
import java.util.Calendar
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
            // Get yesterday's date in "yyyy-MM-dd"
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val yesterday = sdf.format(calendar.time)

            val cutoff = getYesterdayTimestamp()

            // Fetch reminders to archive
            val remindersToArchive = reminderDao.getRemindersBeforeDate(yesterday)

            for (reminder in remindersToArchive) {
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

            // Archive RecentReadAndWatch
            val readsToArchive = recentReadAndWatchDao.getRecentReadAndWatchBeforeDate(cutoff)
            for (read in readsToArchive) {
                val archiveReadNWatch = ArchiveRecentReadAndWatch(
                    id = read.id,
                    moduleId = read.moduleId,
                    timestamp = read.timestamp
                )

                archiveDao.insertArchiveReadAndWatch(
                    archiveReadNWatch
                )
                recentReadAndWatchDao.deleteRecentReadAndWatch(read.id)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun getYesterdayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return calendar.timeInMillis
    }
}
