package dev.adriele.adolescare.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.adriele.adolescare.database.entities.RecentReadAndWatch

@Dao
interface RecentReadAndWatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecent(recent: RecentReadAndWatch)

    @Query("SELECT * FROM recent_read_and_watch")
    suspend fun getAllRecentReadAndWatch(): List<RecentReadAndWatch>
}