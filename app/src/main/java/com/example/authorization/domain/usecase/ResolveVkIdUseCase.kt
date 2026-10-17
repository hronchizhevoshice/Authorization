package com.example.authorization.domain.usecase

import com.example.authorization.domain.repository.VkRepository

class ResolveVkIdUseCase(
    private val vkRepository: VkRepository
) {
    suspend operator fun invoke(input: String): Result<Long> =
        vkRepository.resolveVkId(input)
}