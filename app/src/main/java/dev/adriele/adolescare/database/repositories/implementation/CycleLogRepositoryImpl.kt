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

    override suspend fun updateListsByUserIdAndDate(
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
    ) {
        cycleLogDao.updateListsByUserIdAndDate(
            userId = userId,
            date = date,
            dayCycle = dayCycle,
            symptoms = symptoms,
            sexActivity = sexActivity,
            pregnancyTestResult = pregnancyTestResult,
            mood = mood,
            vaginalDischarge = vaginalDischarge,
            digestionAndStool = digestionAndStool,
            physicalActivity = physicalActivity
        )
    }
}