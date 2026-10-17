package com.example.authorization.domain.repository

interface VkRepository {
    /**
     * Парсит ввод пользователя и возвращает VK ID.
     * @return Result.success(id) или Result.failure(с текстом ошибки)
     */
    suspend fun resolveVkId(input: String): Result<Long>
}