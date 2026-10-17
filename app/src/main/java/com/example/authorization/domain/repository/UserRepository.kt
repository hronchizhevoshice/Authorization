package com.example.authorization.domain.repository

import com.example.authorization.domain.model.FullUser
import com.example.authorization.domain.model.RegisterData
import com.example.authorization.domain.model.User

interface UserRepository {
    suspend fun register(data: RegisterData): Result<User>
    suspend fun login(inn: String, password: String): User?
    suspend fun isInnExists(inn: String): Boolean
    suspend fun getById(id: Long): FullUser?
    suspend fun update(user: FullUser): Result<Unit>
    suspend fun updateVkId(userId: Long, vkId: Long?): Boolean
}