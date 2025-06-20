package dev.adriele.adolescare.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import dev.adriele.adolescare.database.entities.CycleLogEntity
import dev.adriele.adolescare.database.entities.MenstrualCycle
import dev.adriele.adolescare.database.repositories.CycleLogRepository
import kotlinx.coroutines.launch

class CycleLogViewModel(private val repository: CycleLogRepository) : ViewModel() {
    private val _insertStatus = MutableLiveData<Boolean>()
    val insertStatus: LiveData<Boolean> = _insertStatus

    fun insertCycleLog(cycle: CycleLogEntity) {
        viewModelScope.launch {
            try {
                repository.insertCycleLogs(cycle)
                _insertStatus.value = true
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error inserting cycle log", e)
                _insertStatus.value = false
            }
        }
    }

    fun insertCycle(cycle: MenstrualCycle) {
        viewModelScope.launch {
            try {
                repository.insertCycle(cycle)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error inserting cycle log", e)
            }
        }
    }

    fun getLogByDate(userId: String, date: String): LiveData<CycleLogEntity?> = liveData {
        emit(repository.getLogByDate(userId, date))
    }

}