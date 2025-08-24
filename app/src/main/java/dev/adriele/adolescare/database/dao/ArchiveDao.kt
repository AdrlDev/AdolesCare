package dev.adriele.adolescare.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.adriele.adolescare.database.entities.ArchiveRecentReadAndWatch
import dev.adriele.adolescare.database.entities.ArchiveReminder
import dev.adriele.adolescare.model.ArchiveItem

@Dao
interface ArchiveDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArchiveReminder(reminder: ArchiveReminder)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArchiveReadAndWatch(recentReadAndWatch: ArchiveRecentReadAndWatch)

    @Query("""
        SELECT 
            id AS id,
            title AS title,
            message AS message,
            dateTime AS dateTime,
            type AS type
        FROM archive_reminders
        
        UNION
        
        SELECT 
            id AS id,
            'Read/Watched Content' AS title,
            moduleId AS message,
            datetime(timestamp / 1000, 'unixepoch') AS dateTime,
            'ReadAndWatch' AS type
        FROM archive_recent_read_and_watch

        ORDER BY dateTime DESC
    """)
    suspend fun getAllArchiveItems(): List<ArchiveItem>

    @Query("SELECT * FROM archive_reminders WHERE userId = :userId")
    suspend fun getArchiveReminder(userId: String): List<ArchiveReminder>

    @Query("SELECT * FROM archive_recent_read_and_watch")
    suspend fun getArchiveRecentReadNWatch(): List<ArchiveRecentReadAndWatch>
}