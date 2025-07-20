package dev.adriele.adolescare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.adriele.adolescare.database.entities.RecentReadAndWatch
import dev.adriele.adolescare.database.repositories.RecentReadWatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecentReadWatchViewModel(private val repository: RecentReadWatchRepository) : ViewModel() {
    private val _addRecentStatus = MutableLiveData<Pair<Boolean, String>>()
    val addRecentStatus: LiveData<Pair<Boolean, String>> = _addRecentStatus

    private val _recent = MutableLiveData<List<RecentReadAndWatch>>()
    val recent: LiveData<List<RecentReadAndWatch>> get() = _recent

    fun addRecent(recent: RecentReadAndWatch) {
        viewModelScope.launch {
            try {
                repository.addRecent(recent)
                withContext(Dispatchers.Main) {
                    _addRecentStatus.value = Pair(true, recent.moduleId)
                }
            } catch (e: Exception) {
                _addRecentStatus.value = Pair(false, "")
            }
        }
    }

    fun getRecentReadAndWatch() {
        viewModelScope.launch {
            val recent = repository.getRecentReadAndWatch()
            _recent.value = recent
        }
    }

    suspend fun isRecentExist(id: String): Boolean {
        val count = repository.getRecentReadAndWatch(id)
        return count > 0
    }
}