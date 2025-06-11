package dev.adriele.adolescare.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://adrldev.up.railway.app" // Or your LAN IP

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL) // Replace with your PC IP address
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val api: ChatApiService = retrofit.create(ChatApiService::class.java)
}