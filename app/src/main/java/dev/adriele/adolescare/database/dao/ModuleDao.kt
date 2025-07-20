package dev.adriele.adolescare.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.adriele.adolescare.helpers.enums.ModuleContentType
import dev.adriele.adolescare.database.entities.LearningModule

@Dao
interface ModuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(modules: List<LearningModule>)

    @Query("SELECT COUNT(*) FROM modules WHERE id = :moduleId")
    suspend fun moduleExists(moduleId: String): Int

    @Query("SELECT * FROM modules WHERE contentType = :contentType AND LOWER(category) = :category AND orderBy = 0 LIMIT 1")
    suspend fun getAllModules(contentType: ModuleContentType, category: String): List<LearningModule>

    @Query("SELECT * FROM modules WHERE contentType = :contentType ORDER BY orderBy")
    suspend fun getAllVideoModules(contentType: ModuleContentType): List<LearningModule>

    @Query("SELECT * FROM modules WHERE contentType = :contentType AND category LIKE :category ORDER BY orderBy")
    suspend fun getAllModulesByCategory(contentType: ModuleContentType, category: String): List<LearningModule>

    @Query("SELECT * FROM modules WHERE contentType = :contentType AND category LIKE :category AND title LIKE :query ORDER BY orderBy")
    suspend fun searchModule(contentType: ModuleContentType, category: String, query: String): List<LearningModule>

    @Query("SELECT * FROM modules WHERE id = :moduleId LIMIT 1")
    suspend fun getModuleById(moduleId: String): LearningModule?
}