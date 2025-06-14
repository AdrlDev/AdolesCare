package dev.adriele.adolescare.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.adriele.adolescare.ModuleContentType
import dev.adriele.adolescare.database.entities.LearningModule

@Dao
interface ModuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(modules: List<LearningModule>)

    @Query("SELECT * FROM modules WHERE contentType = :contentType")
    suspend fun getAllModules(contentType: ModuleContentType): List<LearningModule>
}