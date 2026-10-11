package com.example.authorization.domain.usecase

import com.example.authorization.domain.model.FullUser
import com.example.authorization.domain.repository.UserRepository

class GetProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Long): FullUser? =
        userRepository.getById(userId)
}