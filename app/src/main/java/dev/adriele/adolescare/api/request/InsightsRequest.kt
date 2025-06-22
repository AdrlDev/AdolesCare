package dev.adriele.adolescare.api.request

data class InsightsRequest(
    val sexDrives: List<String>,
    val moods: List<String>
)
