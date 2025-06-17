package dev.adriele.adolescare.model

import dev.adriele.adolescare.database.entities.LearningModule

data class CategoryModuleGroup(
    val category: String,
    val modules: List<LearningModule>
)
