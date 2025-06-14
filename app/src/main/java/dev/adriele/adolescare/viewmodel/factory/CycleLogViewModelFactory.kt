package dev.adriele.adolescare.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.adriele.adolescare.database.repositories.CycleLogRepository
import dev.adriele.adolescare.viewmodel.CycleLogViewModel

class CycleLogViewModelFactory(private val repository: CycleLogRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CycleLogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CycleLogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
