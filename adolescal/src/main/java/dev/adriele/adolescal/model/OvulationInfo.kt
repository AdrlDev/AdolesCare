package dev.adriele.adolescal.model

import dev.adriele.adolescal.enums.MenstrualPhase

data class OvulationInfo(
    val ovulationDate: String,
    val fertileStart: String,
    val fertileEnd: String,
    val daysUntilOvulation: Long,
    val cycleDay: Int,
    val remarks: CharSequence,
    val phase: MenstrualPhase,
    val isPossiblyPregnant: Boolean = false,
    val nextMenstruationStart: String? = null,
    val nextMenstruationEnd: String? = null
)
