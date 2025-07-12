package dev.adriele.adolescare.helpers.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.Reminder
import dev.adriele.adolescare.helpers.NotificationUtils
import dev.adriele.adolescare.helpers.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DelayedPeriodWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabaseProvider.getDatabase(applicationContext)
            val historyDao = db.menstrualHistoryDao()
            val reminderDao = db.reminderDao()

            val userId = inputData.getString("userId") ?: return@withContext Result.failure()
            val history = historyDao.getLatestHistoryForUser(userId) ?: return@withContext Result.success()

            val lastPeriodDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).parse(
                history.lastPeriodStart ?: return@withContext Result.success()
            ) ?: return@withContext Result.success()

            val cycleDays = (history.cycleIntervalWeeks ?: 4) * 7

            val expectedNextPeriod = Calendar.getInstance().apply {
                time = lastPeriodDate
                add(Calendar.DAY_OF_YEAR, cycleDays)
            }

            val graceDate = Calendar.getInstance().apply {
                time = expectedNextPeriod.time
                add(Calendar.DAY_OF_YEAR, 2)
            }

            val today = Calendar.getInstance()

            if (today.after(graceDate)) {
                val todayStr = Utility.getCurrentDate()
                val title = "Delayed Period Alert"
                val message = "Your period is more than 2 days late. Consider updating your log or consulting a health expert."

                val existing = reminderDao.getReminderByTitleAndDate(userId, title, todayStr)
                if (existing == null) {
                    val reminder = Reminder(
                        userId = userId,
                        title = title,
                        message = message,
                        dateTime = todayStr,
                        type = "PERIOD_ALERT"
                    )
                    reminderDao.insert(reminder)

                    NotificationUtils.showNotification(
                        context = applicationContext,
                        title = title,
                        message = message,
                        userId = userId
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
