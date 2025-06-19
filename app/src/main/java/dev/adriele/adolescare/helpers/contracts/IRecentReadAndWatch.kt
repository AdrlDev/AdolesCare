package dev.adriele.adolescare.helpers.contracts

import dev.adriele.adolescare.helpers.enums.ModuleContentType

interface IRecentReadAndWatch {
    fun onRecentClick(moduleType: ModuleContentType, path: String)
}