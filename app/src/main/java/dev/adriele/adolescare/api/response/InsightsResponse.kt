package dev.adriele.adolescare.api.response

data class InsightsResponse(
    val sexDrives: List<String>,
    val moods: List<String>,
    val symptoms: List<String>,
    val vaginalDischarge: List<String>,
    val digestionAndStool: List<String>,
    val pregnancyTest: List<String>,
    val physicalActivity: List<String>,
    val insights: Insights,
    val cached: Boolean
)

data class Insights(
    val full: String,
    val summary: Summary
)

data class Summary(
    val possibleConditions: List<String>,
    val recommendations: List<String>,
    val warnings: List<String>,
    val notes: String
)
