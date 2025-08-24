package dev.adriele.adolescare.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.adriele.adolescare.database.repositories.ArchiveRepository
import dev.adriele.adolescare.viewmodel.ArchiveViewModel

class ArchiveViewModelFactory(private val repository: ArchiveRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArchiveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArchiveViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
