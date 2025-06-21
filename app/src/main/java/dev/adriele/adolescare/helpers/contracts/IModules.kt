package dev.adriele.adolescare.helpers.contracts

import dev.adriele.adolescare.database.entities.LearningModule

interface IModules {
    interface VIDEO {
        fun onVideoClick(position: Int, path: String)
    }

    interface PDF {
        fun onPdfClick(module: LearningModule)
    }
}