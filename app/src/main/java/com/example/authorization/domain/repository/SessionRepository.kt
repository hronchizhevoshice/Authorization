package com.example.authorization.domain.repository

import com.example.authorization.domain.model.User
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeUser(): Flow<User?>
    suspend fun getUser(): User?
    suspend fun saveUser(user: User)
    suspend fun clear()
}