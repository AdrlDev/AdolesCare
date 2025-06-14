package dev.adriele.adolescare.database.repositories

import dev.adriele.adolescare.database.entities.CycleLogEntity

interface CycleLogRepository {
    suspend fun insertCycleLogs(cycleLogEntity: CycleLogEntity)

    suspend fun getLogByDate(userId: String, date: String): CycleLogEntity?
}