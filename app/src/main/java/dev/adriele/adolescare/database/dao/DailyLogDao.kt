package dev.adriele.adolescare.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.adriele.adolescare.database.entities.DailyLog

@Dao
interface DailyLogDao {
    @Insert
    suspend fun insert(log: DailyLog)

    @Query("SELECT * FROM logs WHERE userId = :userId AND date = :date")
    suspend fun getLog(userId: String, date: String): DailyLog?
}