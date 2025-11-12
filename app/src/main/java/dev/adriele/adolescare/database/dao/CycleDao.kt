package dev.adriele.adolescare.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.adriele.adolescare.database.entities.MenstrualCycle

@Dao
interface CycleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cycle: MenstrualCycle)

    @Query("SELECT * FROM cycles WHERE userId = :userId AND createdAt = :createdAt AND lastPeriodStart = :lmp ORDER BY id DESC LIMIT 1")
    suspend fun getLatestCycle(userId: String, createdAt: String, lmp: String): MenstrualCycle?

    @Query("""
        SELECT * FROM cycles
        WHERE userId = :userId
          AND strftime('%Y-%m', lastPeriodStart) = strftime('%Y-%m', :lmp)
        LIMIT 1
    """)
    suspend fun getCycleByMonth(userId: String, lmp: String): MenstrualCycle?

    @Update
    suspend fun updateCycle(cycle: MenstrualCycle)

    @Query("UPDATE cycles SET lastPeriodStart = :lmp, periodDurationDays = :days, cycleLengthWeeks = :weeks WHERE userId = :userId")
    suspend fun updateCycle(lmp: String, days: Int, weeks: Int, userId: String)

    @Query("SELECT * FROM cycles WHERE userId = :userId ORDER BY id")
    fun getAllCycles(userId: String): LiveData<List<MenstrualCycle>>
}