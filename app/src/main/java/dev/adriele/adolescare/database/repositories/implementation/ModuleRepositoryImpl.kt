package dev.adriele.adolescare.database.repositories.implementation

import dev.adriele.adolescare.helpers.enums.ModuleContentType
import dev.adriele.adolescare.database.dao.ModuleDao
import dev.adriele.adolescare.database.entities.LearningModule
import dev.adriele.adolescare.database.repositories.ModuleRepository

class ModuleRepositoryImpl(private val dao: ModuleDao): ModuleRepository {
    override suspend fun insertModules(modules: List<LearningModule>) {
        dao.insertAll(modules)
    }

    override suspend fun isModuleExist(id: String): Int {
        return dao.moduleExists(id)
    }

    override suspend fun getAllModules(moduleContentType: ModuleContentType, category: String): List<LearningModule> {
        return dao.getAllModules(moduleContentType, category)
    }

    override suspend fun getAllVideoModules(moduleContentType: ModuleContentType): List<LearningModule> {
        return dao.getAllVideoModules(moduleContentType)
    }

    override suspend fun getAllModulesByCategory(
        moduleContentType: ModuleContentType,
        category: String
    ): List<LearningModule> {
        return dao.getAllModulesByCategory(moduleContentType, category)
    }

    override suspend fun searchModule(
        moduleContentType: ModuleContentType,
        category: String,
        query: String
    ): List<LearningModule> {
        return dao.searchModule(moduleContentType, category, "%$query%")
    }

    override suspend fun getModuleById(moduleId: String): LearningModule? {
        return dao.getModuleById(moduleId)
    }

}