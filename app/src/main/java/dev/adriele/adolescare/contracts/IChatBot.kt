package dev.adriele.adolescare.contracts

import dev.adriele.adolescare.api.response.InsightsResponse
import dev.adriele.adolescare.api.response.TipResponse

interface IChatBot {
    fun onResult(result: String)
    interface Tips {
        fun onResult(result: TipResponse)
    }
    interface Insight {
        fun onResult(result: InsightsResponse)
    }
}