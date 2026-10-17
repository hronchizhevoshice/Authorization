package com.example.authorization.data.repository

import com.example.authorization.DatabaseManager
import com.example.authorization.domain.model.FullUser
import com.example.authorization.domain.model.RegisterData
import com.example.authorization.domain.model.User
import com.example.authorization.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserRepositoryImpl : UserRepository {

    override suspend fun register(data: RegisterData): Result<User> =
        withContext(Dispatchers.IO) {
            DatabaseManager.registerUser(data)
        }

    override suspend fun login(inn: String, password: String): User? =
        withContext(Dispatchers.IO) {
            DatabaseManager.loginUser(inn, password)
        }

    override suspend fun isInnExists(inn: String): Boolean =
        withContext(Dispatchers.IO) {
            DatabaseManager.isInnExists(inn)
        }

    override suspend fun getById(id: Long): FullUser? =
        withContext(Dispatchers.IO) {
            DatabaseManager.getUserById(id)
        }

    override suspend fun update(user: FullUser): Result<Unit> =
        withContext(Dispatchers.IO) {
            DatabaseManager.updateUser(user)
        }

    override suspend fun updateVkId(userId: Long, vkId: Long?): Boolean =
        withContext(Dispatchers.IO) {
            DatabaseManager.updateVkId(userId, vkId)
        }
}
