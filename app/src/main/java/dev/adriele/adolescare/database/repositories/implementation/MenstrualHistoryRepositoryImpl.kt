package dev.adriele.adolescare.database.repositories.implementation

import dev.adriele.adolescare.database.dao.MenstrualHistoryDao
import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity
import dev.adriele.adolescare.database.repositories.MenstrualHistoryRepository

class MenstrualHistoryRepositoryImpl(private val menstrualHistoryDao: MenstrualHistoryDao): MenstrualHistoryRepository {
    override suspend fun insertMenstrualHistory(menstrualHistoryEntity: MenstrualHistoryEntity) {
        menstrualHistoryDao.insert(menstrualHistoryEntity)
    }

    override suspend fun updateMenstrualHistory(menstrualHistoryEntity: MenstrualHistoryEntity) {
        menstrualHistoryDao.update(menstrualHistoryEntity)
    }

    override suspend fun getMenstrualHistoryById(userId: String): MenstrualHistoryEntity? {
        return menstrualHistoryDao.getLatestHistoryForUser(userId)
    }
}