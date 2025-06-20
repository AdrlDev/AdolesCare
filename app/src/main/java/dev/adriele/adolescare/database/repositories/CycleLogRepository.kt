package dev.adriele.adolescare.database.repositories

import dev.adriele.adolescare.database.entities.CycleLogEntity
import dev.adriele.adolescare.database.entities.MenstrualCycle

interface CycleLogRepository {
    suspend fun insertCycleLogs(cycleLogEntity: CycleLogEntity)

    suspend fun insertCycle(cycleEntity: MenstrualCycle)

    suspend fun getLogByDate(userId: String, date: String): CycleLogEntity?
}