package com.example.textn.data.api

import com.example.textn.data.model.GeminiRequestBody
import com.example.textn.data.model.GeminiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {

    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    fun getResponseFromGemini(
        @Query("key") apiKey: String,
        @Body requestBody: GeminiRequestBody
    ): Call<GeminiResponse>
}