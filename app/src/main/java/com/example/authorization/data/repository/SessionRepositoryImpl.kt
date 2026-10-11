package com.example.authorization.data.repository

import com.example.authorization.data.local.SessionDataStore
import com.example.authorization.domain.model.User
import com.example.authorization.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

object SessionRepositoryImpl : SessionRepository {

    override fun observeUser(): Flow<User?> = SessionDataStore.observe()

    override suspend fun getUser(): User? = SessionDataStore.get()

    override suspend fun saveUser(user: User) = SessionDataStore.save(user)

    override suspend fun clear() = SessionDataStore.clear()
}