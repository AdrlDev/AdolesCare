package dev.adriele.adolescare.database.repositories.implementation

import dev.adriele.adolescare.helpers.enums.ModuleContentType
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

    override suspend fun getAllModulesByCategory(
        moduleContentType: ModuleContentType,
        category: String
    ): List<LearningModule> {
        return dao.getAllModulesByCategory(moduleContentType, category)
    }

    override suspend fun getModuleById(moduleId: String): LearningModule {
        return dao.getModuleById(moduleId)
    }

}