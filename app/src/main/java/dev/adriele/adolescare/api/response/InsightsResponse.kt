package dev.adriele.adolescare.api.response

data class InsightsResponse(
    val symptoms: List<String>,
    val activities: List<String>,
    val insights: String,
    val cached: Boolean
)
