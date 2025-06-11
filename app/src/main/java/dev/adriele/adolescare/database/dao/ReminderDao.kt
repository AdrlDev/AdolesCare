package dev.adriele.adolescare.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.adriele.adolescare.database.entities.Reminder

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE userId = :userId")
    suspend fun getUserReminders(userId: String): List<Reminder>
}