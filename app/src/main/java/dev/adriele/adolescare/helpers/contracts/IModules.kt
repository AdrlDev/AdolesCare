package dev.adriele.adolescare.helpers.contracts

interface IModules {
    interface VIDEO {
        fun onVideoClick(position: Int, path: String)
    }

    interface PDF {
        fun onPdfClick(position: Int, catPosition: Int, path: String)
    }
}