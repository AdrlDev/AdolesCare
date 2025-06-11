package dev.adriele.adolescare.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.adriele.adolescare.database.repositories.MenstrualHistoryRepository
import dev.adriele.adolescare.viewmodel.MenstrualHistoryViewModel

class MenstrualHistoryViewModelFactory(private val repository: MenstrualHistoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenstrualHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MenstrualHistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
