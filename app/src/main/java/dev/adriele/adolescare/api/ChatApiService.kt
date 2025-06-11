package dev.adriele.adolescare.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ChatApiService {
    @GET("/chat")
    suspend fun getResponse(@Query("query") query: String): Response<OuterResponse>
}