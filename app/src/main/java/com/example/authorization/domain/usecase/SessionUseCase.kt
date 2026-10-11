package com.example.authorization.domain.usecase

import com.example.authorization.domain.model.User
import com.example.authorization.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

class SessionUseCase(
    private val sessionRepository: SessionRepository
) {
    fun observe(): Flow<User?> = sessionRepository.observeUser()
    suspend fun get(): User? = sessionRepository.getUser()
    suspend fun save(user: User) = sessionRepository.saveUser(user)
    suspend fun clear() = sessionRepository.clear()
}