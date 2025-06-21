package dev.adriele.adolescare.helpers.contracts

import dev.adriele.adolescare.database.entities.RecentReadAndWatch
import dev.adriele.adolescare.helpers.enums.ModuleContentType

interface IRecentReadAndWatch {
    fun onRecentClick(moduleType: ModuleContentType, recent: RecentReadAndWatch, path: String)
}