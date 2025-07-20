package dev.adriele.adolescare.database.repositories

import dev.adriele.adolescare.database.entities.RecentReadAndWatch

interface RecentReadWatchRepository {
    suspend fun addRecent(recent: RecentReadAndWatch)

    suspend fun getRecentReadAndWatch(): List<RecentReadAndWatch>
    suspend fun getRecentReadAndWatch(id: String): Int
}