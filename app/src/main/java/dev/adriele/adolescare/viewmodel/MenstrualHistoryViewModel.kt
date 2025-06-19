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
import dev.adriele.adolescare.helpers.enums.MenstrualPhase
import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity
import dev.adriele.adolescare.database.repositories.MenstrualHistoryRepository
import dev.adriele.adolescare.model.OvulationInfo
import dev.adriele.language.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

    fun calculateOvulationDetails(history: MenstrualHistoryEntity, context: Context): OvulationInfo? {
        val lastPeriodStart = history.lastPeriodStart
        val periodDuration = history.periodDurationDays
        val cycleIntervalWeeks = history.cycleIntervalWeeks

        if (lastPeriodStart.isNullOrBlank() || periodDuration == null || cycleIntervalWeeks == null) return null

        Log.e("LMP", "LMP: $lastPeriodStart\nPERIOD_DURATION_IN_DAYS: $periodDuration\nCYCLE_INTERVAL_IN_WEEKS: $cycleIntervalWeeks")

        return try {
            val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).apply { isLenient = false }
            val lastPeriodDate = sdf.parse(lastPeriodStart) ?: return null

            val cycleDays = cycleIntervalWeeks * 7

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val daysSinceLMP = ((today.timeInMillis - lastPeriodDate.time) / (1000 * 60 * 60 * 24)).toInt()
            val cycleIndex = if (daysSinceLMP >= 0) daysSinceLMP / cycleDays else 0

            // Calculate menstruation window
            val menstruationStart = Calendar.getInstance().apply {
                time = lastPeriodDate
                add(Calendar.DAY_OF_YEAR, cycleIndex * cycleDays)
            }
            val menstruationEnd = (menstruationStart.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, periodDuration)
            }

            // Calculate ovulation window
            val ovulationCal = (menstruationStart.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, cycleDays - 14)
            }
            val fertileStart = (ovulationCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -5) }
            val fertileEnd = (ovulationCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }

            // Ensure windows are in the future by advancing them
            while (menstruationEnd.before(today.time)) {
                menstruationStart.add(Calendar.DAY_OF_YEAR, cycleDays)
                menstruationEnd.add(Calendar.DAY_OF_YEAR, cycleDays)
                fertileStart.add(Calendar.DAY_OF_YEAR, cycleDays)
                fertileEnd.add(Calendar.DAY_OF_YEAR, cycleDays)
            }

            val ovulationDate = ovulationCal.time
            val daysUntilOvulation = ((ovulationDate.time - today.timeInMillis) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
            val cycleDay = (daysSinceLMP % cycleDays) + 1

            val todayTime = today.timeInMillis
            val menstrualStartTime = menstruationStart.timeInMillis
            val menstrualEndTime = menstruationEnd.timeInMillis
            val fertileStartTime = fertileStart.timeInMillis
            val fertileEndTime = fertileEnd.timeInMillis

            Log.e("PHASE", "menstrualStartTime: $menstrualStartTime \nmenstrualEndTime: $menstrualEndTime \n" +
                    "fertileStartTime: $fertileStartTime \nfertileEndTime: $fertileEndTime")

            val phase = when {
                todayTime in menstrualStartTime until menstrualEndTime -> MenstrualPhase.MENSTRUAL
                todayTime in menstrualEndTime until fertileStartTime -> MenstrualPhase.FOLLICULAR
                todayTime in fertileStartTime until fertileEndTime -> MenstrualPhase.OVULATION
                todayTime >= fertileEndTime -> MenstrualPhase.LUTEAL
                else -> MenstrualPhase.UNKNOWN
            }

            val ovulationCountdownRemark = if (
                phase == MenstrualPhase.FOLLICULAR &&
                daysUntilOvulation in 1..10
            ) {
                " (${context.getString(R.string.remarks_2)} $daysUntilOvulation ${context.getString(R.string.remarks_2_1)})"
            } else ""

            val fertilityRemark = when (phase) {
                MenstrualPhase.MENSTRUAL -> context.getString(R.string.remarks_3) + " (Low fertility)"
                MenstrualPhase.FOLLICULAR -> "Chance increasing"
                MenstrualPhase.OVULATION -> context.getString(R.string.remarks_1) + " (High fertility)"
                MenstrualPhase.LUTEAL -> "Fertility declining"
                MenstrualPhase.UNKNOWN -> "Cycle day out of range"
            } + ovulationCountdownRemark

            val remarksText = when (phase) {
                MenstrualPhase.MENSTRUAL -> "🔵 $fertilityRemark (Menstrual phase)"
                MenstrualPhase.FOLLICULAR -> "🔵 $fertilityRemark (Follicular phase)"
                MenstrualPhase.OVULATION -> "🟢 $fertilityRemark (Ovulation phase)"
                MenstrualPhase.LUTEAL -> "🔵 $fertilityRemark (Luteal phase)"
                MenstrualPhase.UNKNOWN -> "⚪ Unknown"
            }

            val spannable = SpannableString(remarksText)
            val phaseText = remarksText.substringAfterLast("(").removeSuffix(")")
            val startIdx = remarksText.indexOf("($phaseText)")
            if (startIdx != -1) {
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIdx,
                    startIdx + phaseText.length + 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            return OvulationInfo(
                ovulationDate = sdf.format(ovulationDate),
                fertileStart = sdf.format(fertileStart.time),
                fertileEnd = sdf.format(fertileEnd.time),
                daysUntilOvulation = daysUntilOvulation,
                cycleDay = cycleDay,
                remarks = spannable,
                phase = phase
            )
        } catch (e: Exception) {
            Log.e("LMP", "Error: ${e.message}", e)
            null
        }
    }

    fun loadLatestHistory(userId: String, context: Context) {
        viewModelScope.launch {
            val history = withContext(Dispatchers.IO) {
                repository.getMenstrualHistoryById(userId)
            }

            val result = history?.let { calculateOvulationDetails(it, context) }
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