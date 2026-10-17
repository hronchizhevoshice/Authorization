package com.example.authorization.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VkResolveResponse(
    val response: VkResolveItem? = null,
    val error: VkError? = null
)

@Serializable
data class VkResolveItem(
    @SerialName("object_id") val objectId: Long,
    val type: String
)

@Serializable
data class VkError(
    @SerialName("error_code") val errorCode: Int,
    @SerialName("error_msg") val errorMsg: String
)