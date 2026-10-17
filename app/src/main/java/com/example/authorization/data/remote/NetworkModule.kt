package com.example.authorization.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkModule {

    private const val DADATA_BASE_URL = "https://suggestions.dadata.ru/"
    private const val VK_BASE_URL = "https://api.vk.com/"

    const val DADATA_TOKEN = "9030d41022d88502759c3ba88225a98e9223f1fb"
    const val VK_TOKEN = "vk1.a.OeSL5mL0iCeujT0yPe0g1OwsF6DvSyxtWUHR1QwVPIOpU16yqfRVEEkFueOOGpN3FDG2vjZmZiOEJyUYVmut0f1xqS7FYDLGtzeaG4KGdmpmjGXT_Op8DJUllJ5s7EBa1RI9pxmhFlhyrvm85RYv4za5bVK5_-hw6a5H8NCwYph6KEmWpG3DokSnRw8VdCXbYpPW3M_rhDbGrFdu5YQADA"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(DADATA_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val vkRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(VK_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val dadataApi: DadataApi by lazy {
        retrofit.create(DadataApi::class.java)
    }

    val vkApi: VkApi by lazy {
        vkRetrofit.create(VkApi::class.java)
    }
}