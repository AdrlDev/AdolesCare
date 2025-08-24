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
import dev.adriele.adolescare.database.entities.ArchiveRecentReadAndWatch
import dev.adriele.adolescare.database.entities.ArchiveReminder
import dev.adriele.adolescare.database.repositories.ArchiveRepository
import dev.adriele.adolescare.helpers.worker.ReminderArchiverWorker
import dev.adriele.adolescare.model.ArchiveItem
import dev.adriele.adolescare.model.InsertState
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ArchiveViewModel(private val repository: ArchiveRepository) : ViewModel() {
    private var _archive: MutableLiveData<List<ArchiveItem>> = MutableLiveData()
    val archive: LiveData<List<ArchiveItem>> get() = _archive

    private var _archiveReminder: MutableLiveData<List<ArchiveReminder>> = MutableLiveData()
    val archiveReminder: LiveData<List<ArchiveReminder>> get() = _archiveReminder

    private var _archiveReadNWatch: MutableLiveData<List<ArchiveRecentReadAndWatch>> = MutableLiveData()
    val archiveReadNWatch: LiveData<List<ArchiveRecentReadAndWatch>> get() = _archiveReadNWatch

    private val _insertState = MutableLiveData<InsertState>(InsertState.Idle)
    val insertState: LiveData<InsertState> get() = _insertState

    fun insertArchiveReminder(reminder: ArchiveReminder) {
        viewModelScope.launch {
            _insertState.value = InsertState.Loading
            try {
                repository.insertArchiveReminder(reminder)
                _insertState.value = InsertState.DataToArchive(reminder, null)
                _insertState.value = InsertState.Success
            } catch (e: Exception) {
                _insertState.value = InsertState.Error(e.message ?: "Unknown error")
                Log.e("ViewModelArchive", "Error inserting archive", e)
            }
        }
    }

    fun insertArchiveReadAndWatch(archiveReadAndWatch: ArchiveRecentReadAndWatch) {
        viewModelScope.launch {
            _insertState.value = InsertState.Loading
            try {
                repository.insertArchiveReadAndWatch(archiveReadAndWatch)
                _insertState.value = InsertState.DataToArchive(null, archiveReadAndWatch)
                _insertState.value = InsertState.Success
            } catch (e: Exception) {
                _insertState.value = InsertState.Error(e.message ?: "Unknown error")
                Log.e("ViewModelArchive", "Error inserting archive", e)
            }
        }
    }

    fun getAllArchive() {
        viewModelScope.launch {
            try {
                _archive.value = repository.getAllArchiveItems()
            } catch (e: Exception) {
                _archive.value = emptyList()
                Log.e("ViewModelArchive", "Error fetching archive", e)
            }
        }
    }

    fun getArchiveReminder(userId: String) {
        viewModelScope.launch {
            try {
                _archiveReminder.value = repository.getArchiveReminder(userId)
            } catch (e: Exception) {
                _archiveReminder.value = emptyList()
                Log.e("ViewModelArchive", "Error fetching archive", e)
            }
        }
    }

    fun getArchiveReadNWatch() {
        viewModelScope.launch {
            try {
                _archiveReadNWatch.value = repository.getArchiveRecentReadNWatch()
            } catch (e: Exception) {
                _archiveReadNWatch.value = emptyList()
                Log.e("ViewModelArchive", "Error fetching archive", e)
            }
        }
    }

    fun scheduleReminderArchiverWorker(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderArchiverWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "reminder-archiver",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

}