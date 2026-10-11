package com.example.authorization.data.repository

import com.example.authorization.data.mapper.toCompanyInfoOrNull
import com.example.authorization.data.remote.NetworkModule
import com.example.authorization.data.remote.dto.DadataRequest
import com.example.authorization.domain.model.CompanyInfo
import com.example.authorization.domain.repository.CompanyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object CompanyRepositoryImpl : CompanyRepository {

    private val api = NetworkModule.dadataApi

    override suspend fun findByInn(inn: String): CompanyInfo? =
        withContext(Dispatchers.IO) {
            try {
                val response = api.findByInn(
                    auth = "Token ${NetworkModule.DADATA_TOKEN}",
                    request = DadataRequest(query = inn, count = 1)
                )
                response.toCompanyInfoOrNull()
            } catch (e: IOException) {
                null
            } catch (e: Exception) {
                null
            }
        }
}