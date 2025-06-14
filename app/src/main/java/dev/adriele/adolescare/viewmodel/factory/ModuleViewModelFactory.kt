package dev.adriele.adolescare.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.adriele.adolescare.database.repositories.ModuleRepository
import dev.adriele.adolescare.viewmodel.ModuleViewModel

class ModuleViewModelFactory(private val repository: ModuleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModuleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ModuleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}