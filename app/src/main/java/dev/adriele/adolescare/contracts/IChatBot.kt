package dev.adriele.adolescare.contracts

import dev.adriele.adolescare.api.response.InsightsResponse
import dev.adriele.adolescare.api.response.Sources
import dev.adriele.adolescare.api.response.TipResponse

interface IChatBot {
    fun onResult(result: String, source: List<Sources>? = null)
    interface Tips {
        fun onResult(result: TipResponse)
        fun onError(message: String)
    }
    interface Insight {
        fun onResult(result: InsightsResponse)
        fun onError(message: String)
    }
}