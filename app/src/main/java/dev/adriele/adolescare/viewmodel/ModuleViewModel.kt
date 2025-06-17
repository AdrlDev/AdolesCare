package dev.adriele.adolescare.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.adriele.adolescare.ModuleContentType
import dev.adriele.adolescare.database.entities.LearningModule
import dev.adriele.adolescare.database.repositories.ModuleRepository
import kotlinx.coroutines.launch

class ModuleViewModel(private val repository: ModuleRepository) : ViewModel() {
    private val _insertStatus = MutableLiveData<Boolean>()
    val insertStatus: LiveData<Boolean> = _insertStatus

    private val _modules = MutableLiveData<List<LearningModule>>()
    val modules: LiveData<List<LearningModule>> get() = _modules

    fun insertModules(modules: List<LearningModule>) {
        viewModelScope.launch {
            try {
                repository.insertModules(modules)
                _insertStatus.value = true
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error inserting modules", e)
                _insertStatus.value = false
            }
        }
    }

    fun getAllModules(moduleContentType: ModuleContentType) {
        viewModelScope.launch {
            val modules = repository.getAllModules(moduleContentType)
            _modules.postValue(modules)
        }
    }

    fun getAllModulesByCategory(moduleContentType: ModuleContentType, category: String) {
        viewModelScope.launch {
            val modules = repository.getAllModulesByCategory(moduleContentType, category)
            _modules.postValue(modules)
        }
    }

}