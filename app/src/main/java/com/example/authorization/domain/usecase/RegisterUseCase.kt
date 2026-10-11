package com.example.authorization.domain.usecase

import com.example.authorization.domain.model.RegisterData
import com.example.authorization.domain.model.User
import com.example.authorization.domain.repository.UserRepository

class RegisterUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(data: RegisterData): Result<User> =
        userRepository.register(data)
}