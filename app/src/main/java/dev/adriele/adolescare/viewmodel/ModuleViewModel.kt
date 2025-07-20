package dev.adriele.adolescare.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import dev.adriele.adolescare.helpers.enums.ModuleContentType
import dev.adriele.adolescare.database.entities.LearningModule
import dev.adriele.adolescare.database.repositories.ModuleRepository
import dev.adriele.adolescare.helpers.enums.PDFModulesCategory
import kotlinx.coroutines.launch

class ModuleViewModel(private val repository: ModuleRepository) : ViewModel() {
    private val _modules = MutableLiveData<List<LearningModule>>()
    val modules: LiveData<List<LearningModule>> get() = _modules

    fun insertModules(modules: List<LearningModule>) {
        viewModelScope.launch {
            try {
                repository.insertModules(modules)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error inserting modules", e)
            }
        }
    }

    fun getAllModules(moduleContentType: ModuleContentType) {
        viewModelScope.launch {
            when (moduleContentType) {
                ModuleContentType.VIDEO -> {
                    _modules.postValue(repository.getAllVideoModules(moduleContentType))
                }
                ModuleContentType.PDF -> {
                    val result = mutableListOf<LearningModule>()
                    for (categoryEnum in PDFModulesCategory.entries) {
                        val one = repository.getAllModules(
                            moduleContentType,
                            categoryEnum.category.lowercase()
                        ).firstOrNull()
                        if (one != null) result.add(one)
                    }
                    _modules.postValue(result)
                }
            }
        }
    }

    fun getAllModulesByCategory(moduleContentType: ModuleContentType, category: String) {
        viewModelScope.launch {
            val modules = repository.getAllModulesByCategory(moduleContentType, category)
            _modules.postValue(modules)
        }
    }

    fun searchModule(category: String, query: String) {
        viewModelScope.launch {
            val modules = repository.searchModule(ModuleContentType.PDF, category, "%$query%")
            _modules.postValue(modules)
        }
    }

    fun getModuleByIdLive(moduleId: String): LiveData<LearningModule?> = liveData {
        emit(repository.getModuleById(moduleId))
    }

    suspend fun moduleExistsNow(id: String): Boolean {
        return repository.isModuleExist(id) > 0
    }

}