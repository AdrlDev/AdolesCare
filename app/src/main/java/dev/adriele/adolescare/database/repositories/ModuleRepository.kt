package dev.adriele.adolescare.database.repositories

import dev.adriele.adolescare.ModuleContentType
import dev.adriele.adolescare.database.entities.LearningModule

interface ModuleRepository {
    suspend fun insertModules(modules: List<LearningModule>)

    suspend fun getAllModules(moduleContentType: ModuleContentType): List<LearningModule>
}