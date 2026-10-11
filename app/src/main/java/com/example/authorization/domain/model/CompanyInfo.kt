package com.example.authorization.domain.model

data class CompanyInfo(
    val kpp: String,
    val companyName: String,
    val form: String,
    val ogrn: String,
    val okpo: String,
    val address: String
)