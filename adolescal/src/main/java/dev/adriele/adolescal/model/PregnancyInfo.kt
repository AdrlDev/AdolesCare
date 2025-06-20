package dev.adriele.adolescal.model

data class PregnancyInfo(
    val isPossiblyPregnant: Boolean,
    val aogWeeks: Int?, // null if not pregnant
    val pregnancyRemarks: String
)