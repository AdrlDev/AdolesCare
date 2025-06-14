package dev.adriele.adolescare.database.repositories.implementation

import dev.adriele.adolescare.database.dao.CycleLogDao
import dev.adriele.adolescare.database.entities.CycleLogEntity
import dev.adriele.adolescare.database.repositories.CycleLogRepository

class CycleLogRepositoryImpl(private val cycleLogDao: CycleLogDao): CycleLogRepository {
    override suspend fun insertCycleLogs(cycleLogEntity: CycleLogEntity) {
        cycleLogDao.insert(cycleLogEntity)
    }

    override suspend fun getLogByDate(
        userId: String,
        date: String
    ): CycleLogEntity? {
        return cycleLogDao.getByDate(userId, date)
    }
}