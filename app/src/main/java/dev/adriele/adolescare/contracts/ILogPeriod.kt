package dev.adriele.adolescare.contracts

import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity

interface ILogPeriod {
    fun onSaveNewLog(menstrualHistory: MenstrualHistoryEntity)
}