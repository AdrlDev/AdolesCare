package dev.adriele.adolescare.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.adriele.adolescare.database.entities.CycleLogEntity
import dev.adriele.adolescare.database.entities.MenstrualCycle
import dev.adriele.adolescare.database.repositories.CycleLogRepository
import kotlinx.coroutines.launch

class CycleLogViewModel(private val repository: CycleLogRepository) : ViewModel() {

    fun insertCycleLog(cycle: CycleLogEntity) {
        viewModelScope.launch {
            try {
                repository.insertCycleLogs(cycle)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error inserting cycle log", e)
            }
        }
    }

    fun insertCycle(cycle: MenstrualCycle) {
        viewModelScope.launch {
            try {
                repository.insertCycle(cycle)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error inserting cycle log", e)
            }
        }
    }

    fun getAllCycles(userId: String): LiveData<List<MenstrualCycle>> {
        return repository.getAllCycles(userId)
    }

    suspend fun getLogByDate(userId: String, date: String): CycleLogEntity? {
        return repository.getLogByDate(userId, date)
    }

    suspend fun getMenstrualCycle(userId: String, date: String, lmp: String): MenstrualCycle? {
        return repository.getMenstrualCycle(userId = userId, date = date, lmp = lmp)
    }

    suspend fun getMenstrualCycle(userId: String, lmp: String): MenstrualCycle? {
        return repository.getMenstrualCycle(userId = userId, lmp = lmp)
    }

    fun updateCycle(lmp: String, days: Int, weeks: Int, userId: String) {
        viewModelScope.launch {
            repository.updateCycle(lmp, days, weeks, userId)
        }
    }

    suspend fun getLogByDateNow(userId: String, date: String): CycleLogEntity? {
        return repository.getLogByDate(userId, date)
    }

    fun updateListsByUserIdAndDate(
        userId: String,
        date: String,
        dayCycle: Int,
        symptoms: List<String>? = null,
        sexActivity: List<String>? = null,
        pregnancyTestResult: List<String>? = null,
        mood: List<String>? = null,
        vaginalDischarge: List<String>? = null,
        digestionAndStool: List<String>? = null,
        physicalActivity: List<String>? = null
    ) {
        viewModelScope.launch {
            try {
                repository.updateListsByUserIdAndDate(
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
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating cycle log", e)
            }
        }
    }

}