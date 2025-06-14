package dev.adriele.adolescare.database.repositories

import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity

interface MenstrualHistoryRepository {
    suspend fun insertMenstrualHistory(menstrualHistoryEntity: MenstrualHistoryEntity)

    suspend fun updateMenstrualHistory(menstrualHistoryEntity: MenstrualHistoryEntity)

    suspend fun getMenstrualHistoryById(userId: String) : MenstrualHistoryEntity?
}