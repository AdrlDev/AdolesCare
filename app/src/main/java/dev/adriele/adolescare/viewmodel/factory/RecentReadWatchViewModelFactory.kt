package dev.adriele.adolescare.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.adriele.adolescare.database.repositories.RecentReadWatchRepository
import dev.adriele.adolescare.viewmodel.RecentReadWatchViewModel

class RecentReadWatchViewModelFactory(private val repository: RecentReadWatchRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecentReadWatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecentReadWatchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}