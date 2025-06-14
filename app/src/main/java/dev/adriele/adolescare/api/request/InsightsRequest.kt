package dev.adriele.adolescare.api.request

data class InsightsRequest(
    val symptoms: List<String>,
    val activities: List<String>
)
