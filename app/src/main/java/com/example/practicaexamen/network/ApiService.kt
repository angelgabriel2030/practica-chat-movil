package com.example.practicaexamen.network

import com.example.practicaexamen.data.*
import retrofit2.Call
import retrofit2.http.*



interface ApiService {

    @POST("v1/login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>

    @POST("v1/logout")
    fun logout(@Body userId: Map<String, Long>): Call<Map<String, String>>

    @GET("v1/messages")
    fun getMessages(): Call<List<MessageDTO>>

    @POST("v1/messages")
    fun sendMessage(@Body body: MessageRequest): Call<MessageDTO>
}
