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
    suspend fun getByDate(userId: String, date: String): CycleLogEntity?

    @Query("""
    UPDATE cycle_logs SET 
        symptoms = :symptoms,
        sexActivity = :sexActivity,
        pregnancyTestResult = :pregnancyTestResult,
        mood = :mood,
        vaginalDischarge = :vaginalDischarge,
        digestionAndStool = :digestionAndStool,
        physicalActivity = :physicalActivity
    WHERE userId = :userId AND date = :date AND cycleDay = :dayCycle
""")
    suspend fun updateListsByUserIdAndDate(
        userId: String,
        date: String,
        dayCycle: Int,
        symptoms: List<String>?,
        sexActivity: List<String>?,
        pregnancyTestResult: List<String>?,
        mood: List<String>?,
        vaginalDischarge: List<String>?,
        digestionAndStool: List<String>?,
        physicalActivity: List<String>?
    )

}
