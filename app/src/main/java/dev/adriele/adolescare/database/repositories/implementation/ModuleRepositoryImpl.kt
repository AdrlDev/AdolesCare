package dev.adriele.adolescare.database.repositories.implementation

import dev.adriele.adolescare.ModuleContentType
import dev.adriele.adolescare.database.dao.ModuleDao
import dev.adriele.adolescare.database.entities.LearningModule
import dev.adriele.adolescare.database.repositories.ModuleRepository

class ModuleRepositoryImpl(private val dao: ModuleDao): ModuleRepository {
    override suspend fun insertModules(modules: List<LearningModule>) {
        dao.insertAll(modules)
    }

    override suspend fun getAllModules(moduleContentType: ModuleContentType): List<LearningModule> {
        return dao.getAllModules(moduleContentType)
    }

}