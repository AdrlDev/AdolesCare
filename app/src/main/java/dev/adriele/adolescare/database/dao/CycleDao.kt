package dev.adriele.adolescare.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.adriele.adolescare.database.entities.MenstrualCycle

@Dao
interface CycleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cycle: MenstrualCycle)

    @Query("SELECT * FROM cycles WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    suspend fun getLatestCycle(userId: String): MenstrualCycle?
}