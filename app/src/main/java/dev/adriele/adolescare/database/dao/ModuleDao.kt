package dev.adriele.adolescare.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.adriele.adolescare.database.entities.LearningModule

@Dao
interface ModuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(module: LearningModule)

    @Query("SELECT * FROM modules")
    suspend fun getAll(): List<LearningModule>
}