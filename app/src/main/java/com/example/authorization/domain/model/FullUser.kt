package com.example.authorization.domain.model

data class FullUser(
    val id: Long,
    val email: String,
    val phone: String,
    val password: String,
    val companyName: String,
    val inn: String,
    val kpp: String,
    val form: String,
    val ogrn: String,
    val okpo: String,
    val companyAddress: String,
    val firstname: String,
    val lastname: String,
    val bank: String,
    val bik: String,
    val rs: String,
    val ks: String
)