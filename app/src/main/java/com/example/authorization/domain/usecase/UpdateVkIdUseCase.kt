package com.example.authorization.domain.usecase

import com.example.authorization.domain.repository.UserRepository

class UpdateVkIdUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Long, vkId: Long?): Boolean =
        userRepository.updateVkId(userId, vkId)
}