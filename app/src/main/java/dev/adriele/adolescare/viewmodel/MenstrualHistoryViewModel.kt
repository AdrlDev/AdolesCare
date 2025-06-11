package dev.adriele.adolescare.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity
import dev.adriele.adolescare.database.repositories.MenstrualHistoryRepository
import kotlinx.coroutines.launch

class MenstrualHistoryViewModel(private val repository: MenstrualHistoryRepository) : ViewModel() {
    private val _insertStatus = MutableLiveData<Pair<Boolean, String>>()
    val insertStatus: LiveData<Pair<Boolean, String>> = _insertStatus

    fun insertMenstrualHistory(history: MenstrualHistoryEntity) {
        viewModelScope.launch {
            try {
                repository.insertMenstrualHistory(history)
                _insertStatus.value = Pair(true, history.userId)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error inserting menstrual history", e)
                _insertStatus.value = Pair(false, history.userId)
            }
        }
    }
}