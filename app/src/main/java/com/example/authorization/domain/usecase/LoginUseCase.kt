package com.example.authorization.domain.usecase

import com.example.authorization.domain.model.User
import com.example.authorization.domain.repository.UserRepository

class LoginUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(inn: String, password: String): User? =
        userRepository.login(inn, password)
}