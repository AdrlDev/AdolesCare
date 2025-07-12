package dev.adriele.adolescare.database.dao

import androidx.room.*
import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity

@Dao
interface MenstrualHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: MenstrualHistoryEntity)

    @Update
    suspend fun update(history: MenstrualHistoryEntity)

    @Query("SELECT * FROM menstrual_history WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestHistoryForUser(userId: String): MenstrualHistoryEntity?
}
