package com.example.authorization.data.mapper

import com.example.authorization.data.remote.dto.DadataResponse
import com.example.authorization.domain.model.CompanyInfo

fun DadataResponse.toCompanyInfoOrNull(): CompanyInfo? {
    val suggestion = suggestions.firstOrNull() ?: return null
    val data = suggestion.data ?: return null

    val kpp = data.kpp ?: return null
    if (kpp.isBlank()) return null

    return CompanyInfo(
        kpp = kpp,
        companyName = data.name?.shortWithOpf ?: "",
        form = data.opf?.short ?: "",
        ogrn = data.ogrn ?: "",
        okpo = data.okpo ?: "",
        address = data.address?.value ?: ""
    )
}