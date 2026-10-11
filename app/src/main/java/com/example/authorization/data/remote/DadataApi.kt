package com.example.authorization.data.remote

import com.example.authorization.data.remote.dto.DadataRequest
import com.example.authorization.data.remote.dto.DadataResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface DadataApi {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("suggestions/api/4_1/rs/findById/party")
    suspend fun findByInn(
        @Header("Authorization") auth: String,
        @Body request: DadataRequest
    ): DadataResponse
}