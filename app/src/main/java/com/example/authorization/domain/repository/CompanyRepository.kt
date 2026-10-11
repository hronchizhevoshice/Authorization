package com.example.authorization.domain.repository

import com.example.authorization.domain.model.CompanyInfo

interface CompanyRepository {
    suspend fun findByInn(inn: String): CompanyInfo?
}