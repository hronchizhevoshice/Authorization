package com.example.authorization.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DadataRequest(
    val query: String,
    val count: Int = 1
)

@Serializable
data class DadataResponse(
    val suggestions: List<DadataSuggestion> = emptyList()
)

@Serializable
data class DadataSuggestion(
    val value: String? = null,
    val data: DadataData? = null
)

@Serializable
data class DadataData(
    val kpp: String? = null,
    val ogrn: String? = null,
    val okpo: String? = null,
    val name: DadataName? = null,
    val opf: DadataOpf? = null,
    val address: DadataAddress? = null
)

@Serializable
data class DadataName(
    @SerialName("short_with_opf") val shortWithOpf: String? = null,
    @SerialName("full_with_opf") val fullWithOpf: String? = null
)

@Serializable
data class DadataOpf(
    val short: String? = null
)

@Serializable
data class DadataAddress(
    val value: String? = null
)