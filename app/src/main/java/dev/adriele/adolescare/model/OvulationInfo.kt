package dev.adriele.adolescare.model

import dev.adriele.adolescare.helpers.enums.MenstrualPhase

data class OvulationInfo(
    val ovulationDate: String,
    val fertileStart: String,
    val fertileEnd: String,
    val daysUntilOvulation: Long,
    val cycleDay: Int,
    val remarks: CharSequence,
    val phase: MenstrualPhase
)
