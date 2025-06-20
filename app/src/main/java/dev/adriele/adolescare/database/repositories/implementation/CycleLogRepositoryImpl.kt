package dev.adriele.adolescare.database.repositories.implementation

import dev.adriele.adolescare.database.dao.CycleDao
import dev.adriele.adolescare.database.dao.CycleLogDao
import dev.adriele.adolescare.database.entities.CycleLogEntity
import dev.adriele.adolescare.database.entities.MenstrualCycle
import dev.adriele.adolescare.database.repositories.CycleLogRepository

class CycleLogRepositoryImpl(
    private val cycleLogDao: CycleLogDao,
    private val cycleDao: CycleDao
): CycleLogRepository {
    override suspend fun insertCycleLogs(cycleLogEntity: CycleLogEntity) {
        cycleLogDao.insert(cycleLogEntity)
    }

    override suspend fun insertCycle(cycleEntity: MenstrualCycle) {
        cycleDao.insert(cycleEntity)
    }

    override suspend fun getLogByDate(
        userId: String,
        date: String
    ): CycleLogEntity? {
        return cycleLogDao.getByDate(userId, date)
    }
}