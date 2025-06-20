package dev.adriele.adolescal

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import dev.adriele.adolescal.enums.MenstrualPhase
import dev.adriele.adolescal.model.OvulationInfo
import dev.adriele.adolescal.model.PregnancyInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import dev.adriele.language.R

class OvulationCalculator(
    private val lastPeriodStart: String,
    private val periodDurationDays: Int,
    private val cycleIntervalWeeks: Int,
    private val context: Context
) {
    fun calculate(): OvulationInfo? {
        Log.e("LMP", "LMP: $lastPeriodStart\nPERIOD_DURATION_IN_DAYS: $periodDurationDays\nCYCLE_INTERVAL_IN_WEEKS: $cycleIntervalWeeks")

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

            val menstruationStart = Calendar.getInstance().apply {
                time = lastPeriodDate
                add(Calendar.DAY_OF_YEAR, cycleIndex * cycleDays)
            }
            val menstruationEnd = (menstruationStart.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, periodDurationDays)
            }

            val ovulationCal = (menstruationStart.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, cycleDays - 14)
            }
            val fertileStart = (ovulationCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -5) }
            val fertileEnd = (ovulationCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }

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

            val nextMenstruationStart = (menstruationStart.clone() as Calendar).apply {
                while (before(today)) {
                    add(Calendar.DAY_OF_YEAR, cycleDays)
                }
            }

            val nextMenstruationEnd = (nextMenstruationStart.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, periodDurationDays)
            }

            OvulationInfo(
                ovulationDate = sdf.format(ovulationDate),
                fertileStart = sdf.format(fertileStart.time),
                fertileEnd = sdf.format(fertileEnd.time),
                daysUntilOvulation = daysUntilOvulation,
                cycleDay = cycleDay,
                remarks = spannable,
                phase = phase,
                nextMenstruationStart = sdf.format(nextMenstruationStart.time),
                nextMenstruationEnd = sdf.format(nextMenstruationEnd.time)
            )
        } catch (e: Exception) {
            Log.e("LMP", "Error: ${e.message}", e)
            null
        }
    }

    fun checkPregnancy(): PregnancyInfo {
        val lmpStr = lastPeriodStart

        return try {
            val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val lmpDate = sdf.parse(lmpStr) ?: return PregnancyInfo(false, null, "Invalid LMP format")

            val today = Calendar.getInstance().time
            val daysSinceLMP = ((today.time - lmpDate.time) / (1000 * 60 * 60 * 24)).toInt()
            val aogWeeks = daysSinceLMP / 7

            if (aogWeeks >= 5) {
                PregnancyInfo(
                    isPossiblyPregnant = true,
                    aogWeeks = aogWeeks,
                    pregnancyRemarks = "Possibly pregnant. Estimated AOG: $aogWeeks weeks."
                )
            } else {
                PregnancyInfo(
                    isPossiblyPregnant = false,
                    aogWeeks = null,
                    pregnancyRemarks = "Menstruation may still be regular."
                )
            }
        } catch (e: Exception) {
            PregnancyInfo(false, null, "Error: ${e.message}")
        }
    }

}