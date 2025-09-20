package dev.adriele.adolescare.database.repositories

import androidx.lifecycle.LiveData
import dev.adriele.adolescare.database.entities.CycleLogEntity
import dev.adriele.adolescare.database.entities.MenstrualCycle

interface CycleLogRepository {
    suspend fun insertCycleLogs(cycleLogEntity: CycleLogEntity)

    suspend fun insertCycle(cycleEntity: MenstrualCycle)

    suspend fun getLogByDate(userId: String, date: String): CycleLogEntity?

    suspend fun getMenstrualCycle(userId: String, date: String, lmp: String): MenstrualCycle?

    suspend fun getMenstrualCycle(userId: String, lmp: String): MenstrualCycle?
    suspend fun updateCycle(lmp: String, days: Int, weeks: Int, userId: String)

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

    fun getAllCycles(userId: String): LiveData<List<MenstrualCycle>>

}