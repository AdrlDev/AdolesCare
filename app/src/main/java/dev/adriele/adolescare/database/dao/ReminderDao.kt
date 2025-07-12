package dev.adriele.adolescare.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.adriele.adolescare.database.entities.Reminder

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE userId = :userId")
    suspend fun getUserReminders(userId: String): List<Reminder>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND title = :title AND dateTime = :date LIMIT 1")
    suspend fun getReminderByTitleAndDate(userId: String, title: String, date: String): Reminder?

}