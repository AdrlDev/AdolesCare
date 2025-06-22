package dev.adriele.adolescare.api.response

data class InsightsResponse(
    val sexDrives: List<String>,
    val moods: List<String>,
    val insights: String,
    val cached: Boolean
)
