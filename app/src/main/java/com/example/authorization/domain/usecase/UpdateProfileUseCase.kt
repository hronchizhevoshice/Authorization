package com.example.authorization.domain.usecase

import com.example.authorization.domain.model.FullUser
import com.example.authorization.domain.repository.UserRepository

class UpdateProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: FullUser): Result<Unit> =
        userRepository.update(user)
}