package dev.adriele.adolescare.database.dao

import androidx.room.*
import dev.adriele.adolescare.database.entities.CycleLogEntity

@Dao
interface CycleLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: CycleLogEntity)

    @Query("SELECT * FROM cycle_logs WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllByUserId(userId: Int): List<CycleLogEntity>

    @Query("SELECT * FROM cycle_logs WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getByDate(userId: Int, date: String): CycleLogEntity?
}
