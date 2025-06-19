package dev.adriele.adolescare.database.repositories.implementation

import dev.adriele.adolescare.database.dao.RecentReadAndWatchDao
import dev.adriele.adolescare.database.entities.RecentReadAndWatch
import dev.adriele.adolescare.database.repositories.RecentReadWatchRepository

class RecentReadWatchRepositoryImpl(private val dao: RecentReadAndWatchDao): RecentReadWatchRepository {
    override suspend fun addRecent(recent: RecentReadAndWatch) {
        dao.addRecent(recent)
    }

    override suspend fun getRecentReadAndWatch(): List<RecentReadAndWatch> {
        return dao.getAllRecentReadAndWatch()
    }
}