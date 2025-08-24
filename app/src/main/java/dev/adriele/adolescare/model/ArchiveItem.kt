package dev.adriele.adolescare.model

data class ArchiveItem(
    val id: Long,
    val title: String,
    val message: String,
    val dateTime: String,
    val type: String,
)