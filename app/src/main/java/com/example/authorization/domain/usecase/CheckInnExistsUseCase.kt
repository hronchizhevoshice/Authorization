package com.example.authorization.domain.usecase

import com.example.authorization.domain.repository.UserRepository

class CheckInnExistsUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(inn: String): Boolean =
        userRepository.isInnExists(inn)
}