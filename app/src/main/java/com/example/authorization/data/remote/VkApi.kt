package com.example.authorization.data.remote

import com.example.authorization.data.remote.dto.VkResolveResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface VkApi {
    @GET("method/utils.resolveScreenName")
    suspend fun resolveScreenName(
        @Query("screen_name") screenName: String,
        @Query("access_token") token: String,
        @Query("v") version: String = "5.199"
    ): VkResolveResponse
}