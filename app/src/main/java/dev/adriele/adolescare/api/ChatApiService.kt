package dev.adriele.adolescare.api

import dev.adriele.adolescare.api.request.InsightsRequest
import dev.adriele.adolescare.api.response.InsightsResponse
import dev.adriele.adolescare.api.response.OuterResponse
import dev.adriele.adolescare.api.response.TipResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ChatApiService {
    @GET("/chat")
    suspend fun getResponse(@Query("query") query: String): Response<OuterResponse>

    @GET("/todays-tip")
    suspend fun getTodayTip(): Response<TipResponse>

    @POST("/insights")
    suspend fun getInsights(@Body request: InsightsRequest): Response<InsightsResponse>
}