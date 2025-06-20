package dev.adriele.adolescare.viewmodel

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.adriele.adolescal.OvulationCalculator
import dev.adriele.adolescal.model.OvulationInfo
import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity
import dev.adriele.adolescare.database.repositories.MenstrualHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MenstrualHistoryViewModel(private val repository: MenstrualHistoryRepository) : ViewModel() {
    private val _insertStatus = MutableLiveData<Pair<Boolean, Boolean>>()
    val insertStatus: LiveData<Pair<Boolean, Boolean>> = _insertStatus

    private val _ovulationInfo = MutableLiveData<OvulationInfo?>()
    val ovulationInfo: LiveData<OvulationInfo?> = _ovulationInfo

    private val _mensHistory = MutableLiveData<MenstrualHistoryEntity?>()
    val mensHistory: LiveData<MenstrualHistoryEntity?> = _mensHistory

    fun insertMenstrualHistory(history: MenstrualHistoryEntity) {
        viewModelScope.launch {
            try {
                repository.insertMenstrualHistory(history)
                _insertStatus.value = Pair(true, history.firstPeriodReported)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error inserting menstrual history", e)
                _insertStatus.value = Pair(false, history.firstPeriodReported)
            }
        }
    }

    fun loadLatestHistory(userId: String, context: Context) {
        viewModelScope.launch {
            val history = withContext(Dispatchers.IO) {
                repository.getMenstrualHistoryById(userId)
            }

            val result = history?.let {
                val calculator = OvulationCalculator(it.lastPeriodStart!!, it.periodDurationDays!!, it.cycleIntervalWeeks!!, context)

                val ovulationInfo = calculator.calculate()
                val pregnancyInfo = calculator.checkPregnancy()

                if (pregnancyInfo.isPossiblyPregnant && ovulationInfo != null) {
                    // Override remarks to indicate pregnancy
                    val remarkText = "🟡 ${pregnancyInfo.pregnancyRemarks}"
                    val spannableRemark = SpannableString(remarkText).apply {
                        setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    // Return a modified copy
                    ovulationInfo.copy(remarks = spannableRemark)
                    ovulationInfo.copy(isPossiblyPregnant = true)
                } else {
                    ovulationInfo
                }
            }
            _ovulationInfo.value = result
        }
    }

    fun getMensHistory(userId: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.getMenstrualHistoryById(userId)
            }
            _mensHistory.value = result
        }
    }

}