package dev.adriele.adolescare.api.request

data class InsightsRequest(
    val sexDrives: List<String>,
    val moods: List<String>,
    val symptoms: List<String>,
    val vaginalDischarge: List<String>,
    val digestionAndStool: List<String>,
    val pregnancyTest: List<String>,
    val physicalActivity: List<String>
)
