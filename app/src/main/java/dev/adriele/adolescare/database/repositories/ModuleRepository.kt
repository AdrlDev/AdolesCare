package dev.adriele.adolescare.database.repositories

import dev.adriele.adolescare.helpers.enums.ModuleContentType
import dev.adriele.adolescare.database.entities.LearningModule

interface ModuleRepository {
    suspend fun insertModules(modules: List<LearningModule>)

    suspend fun isModuleExist(id: String): Int

    suspend fun getAllModules(moduleContentType: ModuleContentType, category: String): List<LearningModule>

    suspend fun getAllVideoModules(moduleContentType: ModuleContentType): List<LearningModule>

    suspend fun getAllModulesByCategory(moduleContentType: ModuleContentType, category: String): List<LearningModule>

    suspend fun searchModule(moduleContentType: ModuleContentType, category: String, query: String): List<LearningModule>

    suspend fun getModuleById(moduleId: String): LearningModule?
}