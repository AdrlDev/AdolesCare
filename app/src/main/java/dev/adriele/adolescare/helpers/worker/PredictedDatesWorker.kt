package dev.adriele.adolescare.helpers.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.Reminder
import dev.adriele.adolescare.helpers.NotificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PredictedDatesWorker(
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

            val nextPeriodStart = Calendar.getInstance().apply {
                time = lastPeriodDate
                add(Calendar.DAY_OF_YEAR, cycleDays)
            }

            val ovulationDay = Calendar.getInstance().apply {
                time = lastPeriodDate
                add(Calendar.DAY_OF_YEAR, cycleDays - 14)
            }

            val fertileStart = Calendar.getInstance().apply {
                time = ovulationDay.time
                add(Calendar.DAY_OF_YEAR, -5)
            }

            val fertileEnd = Calendar.getInstance().apply {
                time = ovulationDay.time
                add(Calendar.DAY_OF_YEAR, 1)
            }

            // Insert Reminder if not already inserted
            val format = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

            val reminders = listOf(
                Triple(
                    format.format(nextPeriodStart.time),
                    "Upcoming Period",
                    "Your next period is predicted to start on ${format.format(nextPeriodStart.time)}."
                ),
                Triple(
                    format.format(ovulationDay.time),
                    "Ovulation Day",
                    "You are predicted to ovulate on ${format.format(ovulationDay.time)}."
                ),
                Triple(
                    format.format(fertileStart.time),
                    "Fertile Window",
                    "Your fertile window starts on ${format.format(fertileStart.time)} and ends on ${format.format(fertileEnd.time)}."
                )
            )

            for ((date, title, message) in reminders) {
                val exists = reminderDao.getReminderByTitleAndDate(userId, title, date)
                if (exists == null) {
                    val reminder = Reminder(
                        userId = userId,
                        title = title,
                        message = message,
                        dateTime = date,
                        type = "PREDICTION"
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