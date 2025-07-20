package dev.adriele.adolescare.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.adriele.adolescare.database.entities.Reminder
import dev.adriele.adolescare.database.repositories.ReminderRepository
import dev.adriele.adolescare.helpers.worker.DelayedPeriodWorker
import kotlinx.coroutines.launch

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {
    private var _reminder: MutableLiveData<List<Reminder>> = MutableLiveData()
    val reminder: LiveData<List<Reminder>> get() = _reminder

    fun insertReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                repository.insertReminder(reminder)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error inserting notification", e)
            }
        }
    }

    fun getAllReminder(userId: String) {
        viewModelScope.launch {
            try {
                val result = repository.getAllReminders(userId)
                _reminder.postValue(result)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error getting notifications", e)
                _reminder.postValue(emptyList())
            }
        }
    }

    suspend fun getReminderByTitleAndDate(userId: String, title: String, date: String): Reminder? {
        return repository.getReminderByTitleAndDate(userId, title, date)
    }

    fun scheduleDailyDelayedPeriodWorker(context: Context, userId: String) {
        val input = workDataOf("userId" to userId)

        val request = PeriodicWorkRequestBuilder<DelayedPeriodWorker>(1, java.util.concurrent.TimeUnit.DAYS)
            .setInputData(input)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag("DelayedPeriodWorker")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "CheckDelayedPeriod",
                ExistingPeriodicWorkPolicy.KEEP, // do not reschedule if already exists
                request
            )
    }

}