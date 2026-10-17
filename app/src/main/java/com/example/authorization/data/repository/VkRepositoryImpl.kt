package com.example.authorization.data.repository

import com.example.authorization.data.remote.NetworkModule
import com.example.authorization.domain.repository.VkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VkRepositoryImpl : VkRepository {

    private val api = NetworkModule.vkApi
    private val token = NetworkModule.VK_TOKEN

    override suspend fun resolveVkId(input: String): Result<Long> = withContext(Dispatchers.IO) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.failure(Exception("Введите ссылку или ID"))
        }

        // 1. Чистое число
        if (trimmed.all { it.isDigit() }) {
            return@withContext Result.success(trimmed.toLong())
        }

        // 2. Формат [id123|Имя]
        val bracketRegex = Regex("""^\[id(\d+)\|.*]$""")
        val bracketMatch = bracketRegex.find(trimmed)
        if (bracketMatch != null) {
            return@withContext Result.success(bracketMatch.groupValues[1].toLong())
        }

        // 3. Ссылка с id (vk.com/id123)
        val urlIdRegex = Regex("""id(\d+)""")
        val urlIdMatch = urlIdRegex.find(trimmed)
        if (urlIdMatch != null) {
            return@withContext Result.success(urlIdMatch.groupValues[1].toLong())
        }

        // 4. Короткое имя (screen_name)
        var screenName = trimmed.trimEnd('/').split("/").last()
        screenName = screenName.removePrefix("@")

        if (screenName.isBlank()) {
            return@withContext Result.failure(Exception("Некорректный формат ввода"))
        }

        // 5. Запрос в VK API
        try {
            val response = api.resolveScreenName(
                screenName = screenName,
                token = token
            )
            val error = response.error
            if (error != null) {
                return@withContext Result.failure(Exception("VK: ${error.errorMsg}"))
            }
            val item = response.response
                ?: return@withContext Result.failure(Exception("Пользователь не найден"))

            Result.success(item.objectId)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }
}