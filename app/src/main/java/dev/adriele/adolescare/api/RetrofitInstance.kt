package dev.adriele.adolescare.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val RENDER_BASE_USER = "http://72.60.193.190/adolescare/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(RENDER_BASE_USER) // Replace with your PC IP address
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val api: ChatApiService = retrofit.create(ChatApiService::class.java)
}