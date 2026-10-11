package com.example.authorization.domain.usecase

import com.example.authorization.domain.model.CompanyInfo
import com.example.authorization.domain.repository.CompanyRepository

class FetchCompanyUseCase(
    private val companyRepository: CompanyRepository
) {
    suspend operator fun invoke(inn: String): CompanyInfo? =
        companyRepository.findByInn(inn)
}