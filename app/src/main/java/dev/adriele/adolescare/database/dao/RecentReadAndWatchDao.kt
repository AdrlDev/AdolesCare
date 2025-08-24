package dev.adriele.adolescare.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.adriele.adolescare.database.entities.RecentReadAndWatch
import dev.adriele.adolescare.database.entities.Reminder

@Dao
interface RecentReadAndWatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecent(recent: RecentReadAndWatch)

    @Query("SELECT * FROM recent_read_and_watch")
    suspend fun getAllRecentReadAndWatch(): List<RecentReadAndWatch>

    @Query("DELETE FROM recent_read_and_watch WHERE id = :id")
    suspend fun deleteRecentReadAndWatch(id: Long)

    @Query("SELECT * FROM recent_read_and_watch WHERE timestamp <= :cutoffTimestamp")
    suspend fun getRecentReadAndWatchBeforeDate(cutoffTimestamp: Long): List<RecentReadAndWatch>

    @Query("SELECT COUNT(*) FROM recent_read_and_watch WHERE moduleId = :id")
    suspend fun getRecentReadAndWatch(id: String): Int
}